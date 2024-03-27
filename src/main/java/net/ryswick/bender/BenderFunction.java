package net.ryswick.bender;

import java.util.List;

public class BenderFunction implements BenderCallable {

    private final Statement.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    BenderFunction(Statement.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");

        return null;
    }

    BenderFunction bind(BenderInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new BenderFunction(declaration, environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }
}
