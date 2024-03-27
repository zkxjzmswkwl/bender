package net.ryswick.bender;

import net.ryswick.bender.Expression.Get;
import net.ryswick.bender.Expression.Index;
import net.ryswick.bender.Expression.IndexAssign;
import net.ryswick.bender.Expression.ListLiteral;
import net.ryswick.bender.Expression.Set;
import net.ryswick.bender.Expression.This;

public class AstPrinter implements Expression.Visitor<String> {

    String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return null;
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value == null) return "nada";
        return expression.value.toString();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.right);
    }

    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return null;
    }

    @Override
    public String visitLogicalExpression(Expression.Logical expression) {
        return null;
    }

    @Override
    public String visitCallExpression(Expression.Call expression) {
        return null;
    }

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expression : expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitGetExpression(Get expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGetExpression'");
    }

    @Override
    public String visitSetExpression(Set expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSetExpression'");
    }

    @Override
    public String visitThisExpression(This expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitThisExpression'");
    }

    @Override
    public String visitListLiteralExpression(ListLiteral expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitListLiteralExpression'");
    }

    @Override
    public String visitIndexExpression(Index expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIndexExpression'");
    }

    @Override
    public String visitIndexAssignExpression(IndexAssign expression) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIndexAssignExpression'");
    }
}
