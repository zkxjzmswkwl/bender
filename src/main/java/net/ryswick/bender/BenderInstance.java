package net.ryswick.bender;

import java.util.HashMap;
import java.util.Map;

public class BenderInstance {
    private BenderClass c;
    private final Map<String, Object> fields = new HashMap<>();

    BenderInstance(BenderClass c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "Instance of " + c.name;
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
