package net.ryswick.bender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import net.ryswick.bender.imaging.Imaging;
import net.ryswick.bender.imaging.Position;

public class Interpreter implements Expression.Visitor<Object>,
        Statement.Visitor<Void> {

    public Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();
    static Robot robot = null;

    Interpreter() {
        globals.define("imageRect", new BenderCallable() {
            @Override
            public int arity() {
                return 4;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Double x && arguments.get(1) instanceof Double y
                        && arguments.get(2) instanceof Double w && arguments.get(3) instanceof Double h) {
                    Imaging.imageScreen(new Position(x.intValue(), y.intValue(), w.intValue(), h.intValue()), null);
                } else {
                    Main.error(0, "imageRect requires four numbers.");
                }
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("clock", new BenderCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("moveMouse", new BenderCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (robot == null) {
                    try {
                        robot = new Robot();
                    } catch (Exception e) {
                        Main.error(0, "Robot boom~");
                    }
                }
                if (arguments.get(0) instanceof Double x && arguments.get(1) instanceof Double y) {
                    robot.mouseMove(x.intValue(), y.intValue());
                } else {
                    Main.error(0, "moveMouse requires two numbers.");
                }
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("leftClick", new BenderCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (robot == null) {
                    try {
                        robot = new Robot();
                    } catch (Exception e) {
                        Main.error(0, "Robot boom~");
                    }
                }
                robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("getCursorPos", new BenderCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (robot == null) {
                    try {
                        robot = new Robot();
                    } catch (Exception e) {
                        Main.error(0, "Robot boom~");
                    }
                }

                Point pos = MouseInfo.getPointerInfo().getLocation();
                return String.format("(%d, %d)", pos.x, pos.y);
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluate(expression.value);

        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.name, value);
        } else {
            globals.assign(expression.name, value);
        }
        // environment.assign(expression.name, value);
        return value;
    }

    @Override
    public Object visitListLiteralExpression(Expression.ListLiteral expression) {
        List<Object> list = new ArrayList<>();
        for (Expression element : expression.elements) {
            list.add(evaluate(element));
        }
        return list;
    }

    @Override
    public Object visitIndexExpression(Expression.Index expression) {
        Object object = evaluate(expression.name);
        Object index = evaluate(expression.index);

        if (object instanceof List) {
            List<Object> list = (List<Object>) object;
            return list.get(((Double) index).intValue());
        }
        Main.error(69, "Shit's fucked. TODO: Fix this error message.");
        return null;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case GREATER:
                checkNumberOperands(expression.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expression.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expression.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                if (left instanceof String && right instanceof Double) {
                    return (String) left + stringify(right);
                }

                throw new RuntimeError(expression.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expression.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expression.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // Will not execute.
        return null;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double) right;
        }

        // Will not execute.
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitCaptureStatement(Statement.Capture statement) {
        Object value = evaluate(statement.expression);
        if (value instanceof String val) {
            Imaging.imageScreen(new Position(0, 0, 2540, 1440), val);
            System.out.println("Captured: " + val);
        } else {
            Main.error(0, "Capture statement must be a string.");
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTruthy(evaluate(statement.condition))) {
            execute(statement.body);
        }
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value != null)
            value = evaluate(statement.value);

        throw new Return(value);
    }

    @Override
    public Void visitFuckitStatement(Statement.Fuckit statement) {
        Object value = evaluate(statement.expression);
        if (value instanceof Double val) {
            System.exit(val.intValue());
        } else {
            Main.error(0, "How does one fuck up fuckit?");
            System.exit(65);
        }
        // Will not execute.
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return lookUpVariable(expression.name, expression);
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }

        environment.define(statement.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object left = evaluate(expression.left);

        if (expression.operator.type == EToken.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expression.right);
    }

    @Override
    public Object visitCallExpression(Expression.Call expression) {
        Object callee = evaluate(expression.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof BenderCallable)) {
            throw new RuntimeError(
                    expression.paren, "Can only call functions and classes.");
        }

        BenderCallable function = (BenderCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expression.paren, "Expected " +
                    function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        BenderFunction function = new BenderFunction(statement, environment, false);
        environment.define(statement.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitThisExpression(Expression.This expression) {
        return lookUpVariable(expression.keyword, expression);
    }

    void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null)
            return "nada";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    public void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        Integer distance = locals.get(expression);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        environment.define(statement.name.lexeme, null);

        Map<String, BenderFunction> methods = new HashMap<>();
        for (Statement.Function method : statement.methods) {
            BenderFunction function = new BenderFunction(
                    method,
                    environment,
                    method.name.lexeme.equals("ctor"));
            methods.put(method.name.lexeme, function);
        }
        BenderClass c = new BenderClass(statement.name.lexeme, methods);
        environment.assign(statement.name, c);
        return null;
    }

    @Override
    public Object visitGetExpression(Expression.Get expression) {
        Object object = evaluate(expression.object);
        if (object instanceof BenderInstance) {
            return ((BenderInstance) object).get(expression.name);
        }

        throw new RuntimeError(expression.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpression(Expression.Set expression) {
        Object object = evaluate(expression.object);

        if (!(object instanceof BenderInstance)) {
            throw new RuntimeError(expression.name, "Only instances have fields.");
        }

        Object value = evaluate(expression.value);
        ((BenderInstance) object).set(expression.name, value);
        return value;
    }

    @Override
    public Object visitIndexAssignExpression(Expression.IndexAssign expression) {
        Object object = evaluate(expression.index.name);
        Object index = evaluate(expression.index.index);
        Object value = evaluate(expression.value);

        if (!(object instanceof List)) {
            Main.error(69, "GIGAFUCKED.");
        }

        List<Object> list = (List<Object>) object;
        list.set(((Double) index).intValue(), value);

        return value;
    }
}
