package net.ryswick.bender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.ryswick.bender.EToken.*;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!eofReached()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Statement declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            syncrhonize();
            return null;
        }
    }

    private Statement classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        List<Statement.Function> methods = new ArrayList<>();

        consume(LEFT_BRACE, "Expect '{' before class body.");
        while (!check(RIGHT_BRACE) && !eofReached()) {
            methods.add(function("method"));
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Statement.Class(name, methods);
    }

    private Statement.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();
        return new Statement.Function(name, parameters, body);
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
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(WHILE)) return whileStatement();
        if (match(RETURN)) return returnStatement();
        if (match(CAPTURE)) return captureStatement();
        if (match(FUCKIT)) return fuckitStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private Statement forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Statement initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after condition.");

        Expression increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after loop clause.");

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(
                Arrays.asList(
                    body,
                    new Statement.Expr(increment)
                )
            );
        }

        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);

        if (initializer != null) {
            body = new Statement.Block(
                Arrays.asList(
                    initializer,
                    body
                )
            );
        }

        return body;
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }

    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement fuckitStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after fuckit.");
        return new Statement.Fuckit(value);
    }

    private Statement captureStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after capture()");
        return new Statement.Capture(value);
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();
        return new Statement.While(condition, body);
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
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();
            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable)expression).name;
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Get) {
                Expression.Get get = (Expression.Get)expression;
                return new Expression.Set(get.object, get.name, value);
            } else if (expression instanceof Expression.Index) {
                Expression.Index index = (Expression.Index)expression;
                return new Expression.IndexAssign(index, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
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

    /**
     * Checks if the end of the file has been reached.
     *
     * @return true if the current token is the EOF token, false otherwise.
     */
    private boolean eofReached() {
        return peek().type == EOF;
    }

    /**
     * Returns the current token without consuming it.
     *
     * @return the current token.
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the most recently consumed token.
     *
     * @return the most recently consumed token.
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Parses a comparison expression.
     *
     * @return a new Binary expression representing the comparison.
     */
    private Expression comparison() {
        Expression expression = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Parses a term expression.
     *
     * @return a new Binary expression representing the term.
     */
    private Expression term() {
        Expression expression = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Parses a factor expression.
     *
     * @return a new Binary expression representing the factor.
     */
    private Expression factor() {
        Expression expression = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Parses a unary expression.
     *
     * @return a new Unary expression if the current token is a BANG or MINUS token, otherwise calls the call() method.
     */
    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }
        return call();
    }

    private Expression call() {
        Expression expression = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expression = finishCall(expression);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expression = new Expression.Get(expression, name);
            } else {
                break;
            }
        }
        return expression;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }


    /**
     * <p>
     * Parses a primary expression.
     * </p>
     *
     * <p>
     * Primary expressions in our grammar are the most basic elements,
     * including literals (like numbers, strings, and boolean values),
     * identifiers (variable names), list literals, and parenthesized expressions.
     * </p>
     *
     * <p>  This method checks the current token to determine what kind of expression to parse.</p>
     * <p>If the current token matches a literal token type, it creates a new Literal expression.</p>
     * <p>If the current token is a LEFT_BRACKET, it calls the listLiteral() method to parse a list literal.</p>
     * <p>If the current token is an IDENTIFIER, it calls the variableOrIndexing() method to parse a variable reference or indexing expression.</p>
     * <p>If the current token is a LEFT_PAREN, it parses a parenthesized expression.</p>
     *
     * @return a new Expression representing the parsed primary expression.
     * @throws ParseError if the current token does not match any known primary expression type.
     */
    private Expression primary() {
        if (match(FALSE))   return new Expression.Literal(false);
        if (match(TRUE))   return new Expression.Literal(true);
        if (match(NIL))   return new Expression.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(LEFT_BRACKET)) {
            return listLiteral();
        }

        if (match(THIS)) {
            return new Expression.This(previous());
        }

        if (match(IDENTIFIER)) {
            return variableOrIndexing();
        }

        if (match(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }

    private Expression listLiteral() {
        List<Expression> elements = new ArrayList<>();
        if (!check(RIGHT_BRACKET)) {
            do {
                if (elements.size() >= 255) {
                    error(peek(), "Can't have more than 255 elements in a list literal.");
                }
                elements.add(expression());
            } while (match(COMMA));
        }

        consume(RIGHT_BRACKET, "Expect ']' after list literal.");
        return new Expression.ListLiteral(elements);
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

    private Expression variableOrIndexing() {
        Expression expression = new Expression.Variable(previous());

        while (true) {
            if (match(LEFT_BRACKET)) {
                Expression index = expression();
                consume(RIGHT_BRACKET, "Expect ']' after index.");
                expression = new Expression.Index(expression, index);
            } else {
                break;
            }
        }

        return expression;
    }
}
