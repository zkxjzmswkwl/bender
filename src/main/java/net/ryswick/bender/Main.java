package net.ryswick.bender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        // Load opencv dll.
        System.loadLibrary("lib/opencv_java490");

        if (args.length > 1) {
            System.out.println("bender <source>.ben");
            System.exit(64);
        }
        if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void printAstTest() {
        Expression expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(EToken.MINUS, "-", null, 1),
                        new Expression.Literal(123)),
                new Token(EToken.STAR, "*", null, 1),
                new Expression.Grouping(
                        new Expression.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }

    private static void runFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError)   System.exit(65);
        if (hadRuntimeError) System.exit(70);
}

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.printf("> ");
            String line = reader.readLine();
            if (line == null)   break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == EToken.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[Line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
}