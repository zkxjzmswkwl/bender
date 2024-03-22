package net.ryswick.bender;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
    }

    static class Expression extends Stmt {
        Expression(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expression expression;
    }

    static class Print extends Stmt {
        Print(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final Expression expression;
    }


    abstract <R> R accept(Visitor<R> visitor);
}
