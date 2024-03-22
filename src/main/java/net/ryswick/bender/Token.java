package net.ryswick.bender;

public class Token {
    final EToken type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(EToken type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
