package net.ryswick.bender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ryswick.bender.EToken.*;


public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, EToken>  keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nada", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("holla", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("val", VAR);
        keywords.put("while", WHILE);
        keywords.put("fuckit", FUCKIT);
        keywords.put("capture", CAPTURE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!eofReached()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ']': addToken(RIGHT_BRACKET); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !eofReached()) advance();
//                } else if (match('*')) {
//                    while (peek() != '\n' && !eofReached()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Main.error(line, "Unexpected character");
                    break;
                }
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphanumeric(peek()))  advance();
        String text = source.substring(start, current);
        EToken type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while(isDigit(peek()))  advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        // Walk the subsequent characters until closing quotation is found.
        while (peek() != '"' && !eofReached()) {
            if (peek() == '\n') line ++;
            advance();
        }

        if (eofReached()) {
            Main.error(line, "Failure to terminate string.");
            return;
        }

        // Found it.
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peekNext() {
        if (current +  1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char peek() {
        if (eofReached()) return '\0';
        return source.charAt(current);
    }

    private boolean match(char expected) {
        if (eofReached()) return false;
        if (source.charAt(current) != expected) return false;

//        System.out.println(source.charAt(current));

        current++;
        return true;
    }

    private char advance() {
        current++;
        return source.charAt(current-1);
    }

    private void addToken(EToken type) {
        addToken(type, null);
    }

    private void addToken(EToken type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean eofReached() {
        return current >= source.length();
    }
}
