package net.ryswick.bender;

import java.util.List;
import java.util.Map;

public class BenderClass implements BenderCallable {
    final String name;
    private final Map<String, BenderFunction> methods;

    BenderClass(String name, Map<String, BenderFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public BenderFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        BenderFunction initializer = findMethod("ctor");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        BenderInstance instance = new BenderInstance(this);
        BenderFunction initializer = findMethod("ctor");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }
}
