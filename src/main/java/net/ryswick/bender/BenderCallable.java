package net.ryswick.bender;

import java.util.List;

interface BenderCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
