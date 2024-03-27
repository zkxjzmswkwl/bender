package net.ryswick.bender;

import java.util.List;

public class BenderClass implements BenderCallable {
    final String name;

    BenderClass(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        BenderInstance instance = new BenderInstance(this);
        return instance;
    }
}
