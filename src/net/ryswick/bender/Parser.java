package net.ryswick.bender;

import java.util.ArrayList;
import java.util.List;

import static net.ryswick.bender.EToken.*;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

//    Expression parse() {
//        try {
//            return expression();
//        } catch (ParseError error) {
//            return null;
//        }
//    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!eofReached()) {
            statements.add(declaration());
            statements.add(statement());
        }

        return statements;
    }

    private Statement declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            syncrhonize();
            return null;
        }
    }

    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }

    private Statement statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expr(expression);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !eofReached()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    private Expression expression() {
        return assignment();
//        return equality();
    }

    private Expression assignment() {
        Expression expression = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();
            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable)expression).name;
                System.out.println("DBEUG");
                return new Expression.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expression;
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private boolean match(EToken... types) {
        for (EToken type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(EToken type) {
        if (eofReached()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!eofReached()) current++;
        return previous();
    }

    private boolean eofReached() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Expression comparison() {
        Expression expression = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }
        return primary();
    }

    private Expression primary() {
        if (match(FALSE))   return new Expression.Literal(false);
        if (match(TRUE))   return new Expression.Literal(true);
        if (match(NIL))   return new Expression.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            System.out.println("IDENTIFIER");
            return new Expression.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(EToken type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
    }

    private void syncrhonize() {
        advance();

        while (!eofReached()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
