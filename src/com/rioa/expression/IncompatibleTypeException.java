package com.rioa.expression;

public class IncompatibleTypeException extends Exception {
    public IncompatibleTypeException(String msg) {
        super(msg);
    }
    public IncompatibleTypeException() {
        this("Types not compatible for this operation");
    }
}
