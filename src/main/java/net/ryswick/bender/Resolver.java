package net.ryswick.bender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.ryswick.bender.Expression.ListLiteral;
import net.ryswick.bender.Statement.Capture;
import net.ryswick.bender.Statement.Class;
import net.ryswick.bender.Statement.Fuckit;
import net.ryswick.bender.Statement.Yoink;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType { NONE, FUNCTION, METHOD, INITIALIZER }
    private enum ClassType { NONE, CLASS }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        declare(statement.name);
        if (statement.initializer != null) {
            resolve(statement.initializer);
        }
        define(statement.name);
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression) {
        if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == Boolean.FALSE) {
            Main.error(expression.name, "Cannot read local variable in its own initializer.");
        }
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression) {
        resolve(expression.value);
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.name);
        define(statement.name);

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition);
        resolve(statement.thenBranch);
        if (statement.elseBranch != null) resolve(statement.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            Main.error(statement.keyword, "Cannot return from top level.");
        }

        if (statement.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Main.error(statement.keyword, "Can't return from ctor. The fuck you doin?");
            }
            resolve(statement.value);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.condition);
        resolve(statement.body);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.callee);
        for (Expression argument : expression.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpression(Expression.Get expression) {
        resolve(expression.object);
        return null;
    }

    @Override
    public Void visitSetExpression(Expression.Set expression) {
        resolve(expression.value);
        resolve(expression.object);
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitThisExpression(Expression.This expression) {
        if (currentClass == ClassType.NONE) {
            Main.error(expression.keyword, "Cannot use 'this' outside of a class.");
            return null;
        }
        resolveLocal(expression, expression.keyword);
        return null;
    }

    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Main.error(name, "Variable with this name already declared in this scope.");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visitClassStatement(Class statement) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(statement.name);
        define(statement.name);
        
        beginScope();
        scopes.peek().put("this", true);

        for (Statement.Function method : statement.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("ctor")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        endScope();
        currentClass = enclosingClass;

        return null;
    }

    @Override
    public Void visitFuckitStatement(Fuckit statement) {
        // TODO: Implement fuckit resolve zzz.
        throw new UnsupportedOperationException("Unimplemented method 'visitFuckitStatement'");
    }

    @Override
    public Void visitCaptureStatement(Capture statement) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCaptureStatement'");
    }

    @Override
    public Void visitListLiteralExpression(Expression.ListLiteral expression) {
        for (Expression element: expression.elements) {
            resolve(element);
        }
        return null;
    }

    @Override
    public Void visitIndexExpression(Expression.Index expression) {
        resolve(expression.name);
        resolve(expression.index);
        return null;
    }

    @Override
    public Void visitIndexAssignExpression(Expression.IndexAssign expression) {
        resolve(expression.index);
        resolve(expression.value);
        return null;
    }

    @Override
    public Void visitYoinkStatement(Yoink statement) {
        return null;
    }
}
