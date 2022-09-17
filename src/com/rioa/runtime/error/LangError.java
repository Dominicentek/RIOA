package com.rioa.runtime.error;

import com.rioa.runtime.RIOARuntime;

public class LangError {
    public static final LangError SYNTAX = new LangError("SyntaxError");
    public static final LangError DEFINE = new LangError("DefineError");
    public static final LangError RANGE = new LangError("RangeError");
    public static final LangError TYPE = new LangError("TypeError");
    public static final LangError IO = new LangError("IOError");
    public static final LangError GRAPHICS = new LangError("GraphicsError");
    public static final LangError INTERNAL = new LangError("InternalError");
    private final String name;
    public LangError(String name) {
        this.name = name;
    }
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
        return name;
    }
}
