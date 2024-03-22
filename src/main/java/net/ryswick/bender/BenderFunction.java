package net.ryswick.bender;

import java.util.List;

public class BenderFunction implements BenderCallable {

    private final Statement.Function declaration;

    BenderFunction(Statement.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }
}
