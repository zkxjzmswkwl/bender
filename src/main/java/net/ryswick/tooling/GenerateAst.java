package net.ryswick.tooling;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("gen_ast <dir>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expression", Arrays.asList(
            "Assign   : Token name, Expression value",
            "Binary   : Expression left, Token operator, Expression right",
            "Call     : Expression callee, Token paren, List<Expression> arguments",
            "Grouping : Expression expression",
            "Literal  : Object value",
            "Logical  : Expression left, Token operator, Expression right",
            "Unary    : Token operator, Expression right",
            "Variable : Token name"
        ));

        defineAst(outputDir, "Statement", Arrays.asList(
            "Block      : List<Statement> statements",
            "Expr       : Expression expression",
            "Function   : Token name, List<Token> params, List<Statement> body",
            "If         : Expression condition, Statement thenBranch, Statement elseBranch",
            "Print      : Expression expression",
            "Fuckit     : Expression expression",
            "Capture    : Expression expression",
            "Return     : Token keyword, Expression value",
            "While      : Expression condition, Statement body",
            "Var        : Token name, Expression initializer"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        Files.createDirectories(Paths.get(outputDir));
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package net.ryswick.bender;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // Base accept()
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");
        writer.println("        " + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // Visitor
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println();
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }
        writer.println("    }\n");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println(
                    "        R visit" + typeName + baseName +
                            "(" + typeName + " " + baseName.toLowerCase() +
                            ");"
            );
        }

        writer.println("    }\n");
    }
}
