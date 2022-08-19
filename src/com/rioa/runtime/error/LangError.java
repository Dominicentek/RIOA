package com.rioa.runtime.error;

import com.rioa.runtime.RIOARuntime;

public enum LangError {
    SYNTAX,
    DEFINE,
    RANGE,
    TYPE,
    IO {
        public String getName() {
            return "IOError";
        }
    },
    GRAPHICS,
    INTERNAL;
    public void report(String msg) {
        System.out.println(getName() + ": " + msg);
        System.exit(msg.hashCode());
    }
    public void report(String msg, int lineNumber, int columnNumber) {
        System.out.println(getName() + ": " + msg + " (" + lineNumber + ";" + columnNumber + ")");
        System.exit(msg.hashCode());
    }
    public void report(RIOARuntime context, String msg) {
        if (context.errorCallbackStack.isEmpty()) {
            System.out.println(getName() + ": " + msg);
            System.exit(msg.hashCode());
        }
        context.errorCallbackStack.peek().callback(getName(), msg, 0, 0);
    }
    public void report(RIOARuntime context, String msg, int lineNumber, int columnNumber) {
        if (context.errorCallbackStack.isEmpty()) {
            System.out.println(getName() + ": " + msg + " (" + lineNumber + ";" + columnNumber + ")");
            System.exit(msg.hashCode());
        }
        context.errorCallbackStack.peek().callback(getName(), msg, lineNumber, columnNumber);
    }
    public String getName() {
        String name = name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Error";
    }
}
