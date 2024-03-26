package net.ryswick.bender;

import java.util.List;

abstract class Statement {
    interface Visitor<R> {
        R visitBlockStatement(Block statement);
        R visitClassStatement(Class statement);
        R visitExprStatement(Expr statement);
        R visitFunctionStatement(Function statement);
        R visitIfStatement(If statement);
        R visitPrintStatement(Print statement);
        R visitFuckitStatement(Fuckit statement);
        R visitCaptureStatement(Capture statement);
        R visitReturnStatement(Return statement);
        R visitWhileStatement(While statement);
        R visitVarStatement(Var statement);
    }

    static class Block extends Statement {
        Block(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }

        final List<Statement> statements;
    }

    static class Class extends Statement {
        Class(Token name, List<Statement.Function> methods) {
            this.name = name;
            this.methods = methods;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStatement(this);
        }

        final Token name;
        final List<Statement.Function> methods;
    }

    static class Expr extends Statement {
        Expr(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStatement(this);
        }

        final Expression expression;
    }

    static class Function extends Statement {
        Function(Token name, List<Token> params, List<Statement> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStatement(this);
        }

        final Token name;
        final List<Token> params;
        final List<Statement> body;
    }

    static class If extends Statement {
        If(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }

        final Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;
    }

    static class Print extends Statement {
        Print(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }

        final Expression expression;
    }

    static class Fuckit extends Statement {
        Fuckit(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFuckitStatement(this);
        }

        final Expression expression;
    }

    static class Capture extends Statement {
        Capture(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCaptureStatement(this);
        }

        final Expression expression;
    }

    static class Return extends Statement {
        Return(Token keyword, Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStatement(this);
        }

        final Token keyword;
        final Expression value;
    }

    static class While extends Statement {
        While(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }

        final Expression condition;
        final Statement body;
    }

    static class Var extends Statement {
        Var(Token name, Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStatement(this);
        }

        final Token name;
        final Expression initializer;
    }


    abstract <R> R accept(Visitor<R> visitor);
}
