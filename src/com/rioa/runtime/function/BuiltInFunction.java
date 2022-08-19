package com.rioa.runtime.function;

import com.rioa.runtime.error.LangError;
import com.rioa.runtime.RIOARuntime;
import com.rioa.runtime.variable.Variable;

public abstract class BuiltInFunction extends Function {
    public BuiltInFunction(FunctionParameter... params) {
        super(null, params);
    }
    public abstract Variable run(RIOARuntime context);
    public void report(RIOARuntime context, LangError error, String msg) {
        error.report(context, msg, context.getCallLineNumber(), context.getCallColumnNumber());
    }
}
