package com.rioa.runtime.codeblock;

import com.rioa.runtime.RIOARuntime;
import com.rioa.runtime.error.LangError;
import com.rioa.runtime.variable.Variable;
import com.rioa.runtime.variable.VariableType;
import com.rioa.token.Token;
import java.util.HashMap;

public class CatchCodeBlock extends CodeBlock {
    public String errorname;
    public String msg;
    public String linenum;
    public String columnnum;
    public CatchCodeBlock(Token[] tokens, String errorname, String msg, String linenum, String columnnum) {
        super(tokens);
        this.errorname = errorname;
        this.msg = msg;
        this.linenum = linenum;
        this.columnnum = columnnum;
    }
    public CodeBlockResult run(RIOARuntime context, String name, String message, int lineNumber, int columnNumber) {
        context.callStack.peek().scope.push(new HashMap<>());
        context.setVariable(errorname, new Variable(name, VariableType.STRING));
        context.setVariable(msg, new Variable(message, VariableType.STRING));
        context.setVariable(linenum, new Variable((double)lineNumber, VariableType.NUMBER));
        context.setVariable(columnnum, new Variable((double)columnNumber, VariableType.NUMBER));
        CodeBlockResult result = runInternal(context);
        context.callStack.peek().scope.pop();
        return result;
    }
}
