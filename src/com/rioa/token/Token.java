package com.rioa.token;

public class Token {
    public Object value;
    public TokenType type;
    public boolean endOfLine;
    public int columnNumber;
    public int lineNumber;
    public boolean is(Object value, TokenType type) {
        return value.equals(this.value) && type == this.type;
    }
    public String toString() {
        return "[" + lineNumber + ";" + columnNumber + "] TYPE=" + type + " EOL=" + endOfLine + " VAL='" + value + "'";
    }
}
