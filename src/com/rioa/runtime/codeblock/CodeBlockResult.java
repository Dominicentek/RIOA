package com.rioa.runtime.codeblock;

import com.rioa.runtime.variable.Variable;

public class CodeBlockResult {
    public static final String END_METHOD_END_OF_CODE_BLOCK = "eocb";
    public static final String END_METHOD_RETURN = "return";
    public static final String END_METHOD_CONTINUE = "continue";
    public static final String END_METHOD_BREAK = "break";
    public String endMethod = END_METHOD_END_OF_CODE_BLOCK;
    public Variable returnVariable = new Variable();
    public int lineNumber = 0;
    public int columnNumber = 0;
    public CodeBlockResult(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    public CodeBlockResult setEndMethod(String endMethod) {
        this.endMethod = endMethod;
        return this;
    }
    public CodeBlockResult setReturnVariable(Variable returnVariable) {
        this.returnVariable = returnVariable;
        return this;
    }
}
