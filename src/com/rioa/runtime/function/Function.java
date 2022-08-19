package com.rioa.runtime.function;

import com.rioa.runtime.error.LangError;
import com.rioa.runtime.RIOARuntime;
import com.rioa.runtime.codeblock.CodeBlock;
import com.rioa.runtime.codeblock.CodeBlockResult;
import com.rioa.runtime.variable.Variable;
import com.rioa.token.Token;

public class Function {
    public CodeBlock codeBlock;
    public FunctionParameter[] params;
    public Function(Token[] tokens, FunctionParameter... params) {
        this.codeBlock = new CodeBlock(tokens);
        this.params = params;
    }
    public Variable run(RIOARuntime context) {
        CodeBlockResult result = codeBlock.run(context);
        if (result.endMethod.equals(CodeBlockResult.END_METHOD_CONTINUE) || result.endMethod.equals(CodeBlockResult.END_METHOD_BREAK)) LangError.SYNTAX.report(result.endMethod + " not allowed here", result.lineNumber, result.columnNumber);
        return result.returnVariable;
    }
}
