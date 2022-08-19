package com.rioa.runtime.codeblock;

import com.rioa.expression.Expression;
import com.rioa.expression.Operator;
import com.rioa.runtime.error.ErrorCallback;
import com.rioa.runtime.error.LangError;
import com.rioa.runtime.RIOARuntime;
import com.rioa.runtime.variable.Variable;
import com.rioa.runtime.variable.VariableType;
import com.rioa.token.Token;
import com.rioa.token.TokenType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static com.rioa.runtime.RIOARuntime.Value;

public class CodeBlock {
    private Token[] tokens;
    public boolean stop;
    public CodeBlock(Token[] tokens) {
        this.tokens = tokens;
    }
    public CodeBlockResult run(RIOARuntime context) {
        context.callStack.peek().scope.push(new HashMap<>());
        CodeBlockResult result = runInternal(context);
        context.callStack.peek().scope.pop();
        return result;
    }
    protected CodeBlockResult runInternal(RIOARuntime context) {
        try {
            Expression rioaExpression = new Expression();
            for (int i = 0; i < tokens.length; i++) {
                if (stop) return new CodeBlockResult(0, 0);
                Token token = tokens[i];
                if (token.type != TokenType.WORD) LangError.SYNTAX.report("Word expected", token.lineNumber, token.columnNumber);
                String keyword = (String)token.value;
                if (keyword.equals(RIOARuntime.KW_FUNC) || keyword.equals(RIOARuntime.KW_IMPORT) || keyword.equals(RIOARuntime.KW_TRUE) || keyword.equals(RIOARuntime.KW_FALSE)) LangError.SYNTAX.report(keyword + " not allowed here", token.lineNumber, token.columnNumber);
                else if (keyword.equals(RIOARuntime.KW_RETURN)) {
                    if (token.endOfLine) return new CodeBlockResult(token.lineNumber, token.columnNumber).setEndMethod(CodeBlockResult.END_METHOD_RETURN);
                    ArrayList<Token> expressionTokens = new ArrayList<>();
                    int j = i + 1;
                    for (; j < tokens.length; j++) {
                        expressionTokens.add(tokens[j]);
                        if (tokens[j].endOfLine) break;
                    }
                    return new CodeBlockResult(token.lineNumber, token.columnNumber).setEndMethod(CodeBlockResult.END_METHOD_RETURN).setReturnVariable(context.getValue(expressionTokens));
                }
                else if (keyword.equals(RIOARuntime.KW_WHILE)) {
                    rioaExpression = new Expression();
                    ArrayList<Token> expressionTokens = new ArrayList<>();
                    int j = i + 1;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) break;
                        expressionTokens.add(tokens[j]);
                    }
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    i++;
                    j++;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    while (true) {
                        Variable condition = context.getValue(expressionTokens);
                        if (condition.type != VariableType.BOOLEAN) LangError.TYPE.report("Condition is not a boolean", tokens[i].lineNumber, tokens[i].columnNumber);
                        if (!(boolean)condition.value) break;
                        CodeBlockResult result = block.run(context);
                        if (result.endMethod.equals(CodeBlockResult.END_METHOD_CONTINUE)) continue;
                        if (result.endMethod.equals(CodeBlockResult.END_METHOD_BREAK)) {
                            if (result.returnVariable.type != VariableType.NUMBER) LangError.TYPE.report("Number expected", result.lineNumber, result.columnNumber);
                            int breaks = (int)((double)result.returnVariable.value);
                            if (breaks <= 1) break;
                            return result.setReturnVariable(new Variable((double)breaks - 1, VariableType.NUMBER));
                        }
                        if (result.endMethod.equals(CodeBlockResult.END_METHOD_RETURN)) return result;
                    }
                    i = j;
                }
                else if (keyword.equals(RIOARuntime.KW_IF)) {
                    rioaExpression = new Expression();
                    ArrayList<Token> expressionTokens = new ArrayList<>();
                    int j = i + 1;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) break;
                        expressionTokens.add(tokens[j]);
                    }
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    i++;
                    j++;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    Variable condition = context.getValue(expressionTokens);
                    if (condition.type != VariableType.BOOLEAN) LangError.TYPE.report("Condition is not a boolean", tokens[i].lineNumber, tokens[i].columnNumber);
                    if ((boolean)condition.value) {
                        CodeBlockResult result = block.run(context);
                        if (!result.endMethod.equals(CodeBlockResult.END_METHOD_END_OF_CODE_BLOCK)) return result;
                    }
                    rioaExpression.addUnit(condition);
                    i = j;
                }
                else if (keyword.equals(RIOARuntime.KW_ELSE)) {
                    if (rioaExpression.isEmpty()) LangError.SYNTAX.report("else without if", token.lineNumber, token.columnNumber);
                    boolean condition = true;
                    i++;
                    if (tokens[i].is(RIOARuntime.KW_IF, TokenType.WORD)) {
                        ArrayList<Token> expressionTokens = new ArrayList<>();
                        int j = i + 1;
                        for (; j < tokens.length; j++) {
                            if (tokens[j].is('{', TokenType.SYMBOL)) break;
                            expressionTokens.add(tokens[j]);
                        }
                        Variable cond = context.getValue(expressionTokens);
                        if (cond.type != VariableType.BOOLEAN) LangError.TYPE.report("Condition is not a boolean", tokens[i].lineNumber, tokens[i].columnNumber);
                        condition = (boolean)cond.value;
                        i = j;
                    }
                    else if (!tokens[i].is('{', TokenType.SYMBOL)) LangError.SYNTAX.report("Code block expected", tokens[i].lineNumber, tokens[i].columnNumber);
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    int j = i + 1;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    if (condition && !(boolean)rioaExpression.getLastUnit().asVariable().value) {
                        CodeBlockResult result = block.run(context);
                        rioaExpression = new Expression();
                        rioaExpression.addUnit(new Variable(condition, VariableType.BOOLEAN));
                        if (!result.endMethod.equals(CodeBlockResult.END_METHOD_END_OF_CODE_BLOCK)) return result;
                    }
                    i = j;
                }
                else if (keyword.equals(RIOARuntime.KW_AND)) {
                    if (rioaExpression.isEmpty()) LangError.SYNTAX.report("and without if", token.lineNumber, token.columnNumber);
                    ArrayList<Token> expressionTokens = new ArrayList<>();
                    i++;
                    if (!tokens[i].is("if", TokenType.WORD)) LangError.SYNTAX.report("'if' expected", tokens[i].lineNumber, tokens[i].columnNumber);
                    int j = i + 1;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) break;
                        expressionTokens.add(tokens[j]);
                    }
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    i++;
                    j++;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    Variable condition = context.getValue(expressionTokens);
                    if (condition.type != VariableType.BOOLEAN) LangError.TYPE.report("Condition is not a boolean", tokens[i].lineNumber, tokens[i].columnNumber);
                    if ((boolean)condition.value) {
                        CodeBlockResult result = block.run(context);
                        if (!result.endMethod.equals(CodeBlockResult.END_METHOD_END_OF_CODE_BLOCK)) return result;
                    }
                    rioaExpression.addUnit(Operator.CONDITIONAL_AND);
                    rioaExpression.addUnit(condition);
                    i = j;
                }
                else if (keyword.equals(RIOARuntime.KW_OR)) {
                    if (rioaExpression.isEmpty()) LangError.SYNTAX.report("or without if", token.lineNumber, token.columnNumber);
                    ArrayList<Token> expressionTokens = new ArrayList<>();
                    i++;
                    if (!tokens[i].is("if", TokenType.WORD)) LangError.SYNTAX.report("'if' expected", tokens[i].lineNumber, tokens[i].columnNumber);
                    int j = i + 1;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) break;
                        expressionTokens.add(tokens[j]);
                    }
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    i++;
                    j++;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    Variable condition = context.getValue(expressionTokens);
                    if (condition.type != VariableType.BOOLEAN) LangError.TYPE.report("Condition is not a boolean", tokens[i].lineNumber, tokens[i].columnNumber);
                    if ((boolean)condition.value) {
                        CodeBlockResult result = block.run(context);
                        if (!result.endMethod.equals(CodeBlockResult.END_METHOD_END_OF_CODE_BLOCK)) return result;
                    }
                    rioaExpression.addUnit(Operator.CONDITIONAL_OR);
                    rioaExpression.addUnit(condition);
                    i = j;
                }
                else if (keyword.equals(RIOARuntime.KW_RUN)) {
                    if (rioaExpression.isEmpty()) LangError.SYNTAX.report("run without if", tokens[i].lineNumber, tokens[i].columnNumber);
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    if (!tokens[i + 1].is('{', TokenType.SYMBOL)) LangError.SYNTAX.report("Code block expected", tokens[i + 1].lineNumber, tokens[i + 1].columnNumber);
                    int j = i + 2;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    if ((boolean)rioaExpression.calculate().value) {
                        CodeBlockResult result = block.run(context);
                        if (!result.endMethod.equals(CodeBlockResult.END_METHOD_END_OF_CODE_BLOCK)) return result;
                    }
                    rioaExpression = new Expression();
                    i = j;
                }
                else if (keyword.equals(RIOARuntime.KW_TRY)) {
                    ArrayList<Token> codeBlock = new ArrayList<>();
                    int layer = 0;
                    if (!tokens[i + 1].is('{', TokenType.SYMBOL)) LangError.SYNTAX.report("Code block expected", tokens[i + 1].lineNumber, tokens[i + 1].columnNumber);
                    int j = i + 2;
                    for (; j < tokens.length; j++) {
                        if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                        if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                        if (layer == -1) break;
                        codeBlock.add(tokens[j]);
                    }
                    CodeBlock block = new CodeBlock(codeBlock.toArray(new Token[0]));
                    i = j + 1;
                    if (tokens[i].is(RIOARuntime.KW_CATCH, TokenType.WORD)) {
                        ArrayList<Token> catchBlock = new ArrayList<>();
                        layer = 0;
                        i++;
                        if (tokens[i].type != TokenType.WORD) LangError.SYNTAX.report("Word expected", tokens[i].lineNumber, tokens[i].columnNumber);
                        String errname = (String)tokens[i].value;
                        i++;
                        if (tokens[i].type != TokenType.WORD) LangError.SYNTAX.report("Word expected", tokens[i].lineNumber, tokens[i].columnNumber);
                        String errmsg = (String)tokens[i].value;
                        i++;
                        if (tokens[i].type != TokenType.WORD) LangError.SYNTAX.report("Word expected", tokens[i].lineNumber, tokens[i].columnNumber);
                        String ln = (String)tokens[i].value;
                        i++;
                        if (tokens[i].type != TokenType.WORD) LangError.SYNTAX.report("Word expected", tokens[i].lineNumber, tokens[i].columnNumber);
                        String cn = (String)tokens[i].value;
                        if (!tokens[i + 1].is('{', TokenType.SYMBOL)) LangError.SYNTAX.report("Code block expected", tokens[i + 1].lineNumber, tokens[i + 1].columnNumber);
                        j = i + 2;
                        for (; j < tokens.length; j++) {
                            if (tokens[j].is('{', TokenType.SYMBOL)) layer++;
                            if (tokens[j].is('}', TokenType.SYMBOL)) layer--;
                            if (layer == -1) break;
                            catchBlock.add(tokens[j]);
                        }
                        CatchCodeBlock catchCodeBlock = new CatchCodeBlock(catchBlock.toArray(new Token[0]), errname, errmsg, ln, cn);
                        final Value<CodeBlockResult> result = new Value<>(new CodeBlockResult(token.lineNumber, token.columnNumber));
                        final Value<Boolean> noError = new Value<>(true);
                        context.errorCallbackStack.push((name, msg, lineNumber, columnNumber) -> {
                            block.stop = true;
                            result.value = catchCodeBlock.run(context, name, msg, lineNumber, columnNumber);
                            noError.value = false;
                        });
                        CodeBlockResult tryResult = block.run(context);
                        if (noError.value) result.value = tryResult;
                        context.errorCallbackStack.pop();
                        if (!result.value.endMethod.equals(CodeBlockResult.END_METHOD_END_OF_CODE_BLOCK)) return result.value;
                        i = j;
                    }
                }
                else if (keyword.equals(RIOARuntime.KW_CATCH)) {
                    LangError.SYNTAX.report("catch without try", token.lineNumber, token.columnNumber);
                }
                else if (keyword.equals(RIOARuntime.KW_CONTINUE)) {
                    return new CodeBlockResult(token.lineNumber, token.columnNumber).setEndMethod(CodeBlockResult.END_METHOD_CONTINUE);
                }
                else if (keyword.equals(RIOARuntime.KW_BREAK)) {
                    if (token.endOfLine) return new CodeBlockResult(token.lineNumber, token.columnNumber).setEndMethod(CodeBlockResult.END_METHOD_BREAK);
                    ArrayList<Token> tokens = new ArrayList<>();
                    for (i++; i < this.tokens.length; i++) {
                        tokens.add(this.tokens[i]);
                        if (this.tokens[i].endOfLine) break;
                    }
                    return new CodeBlockResult(token.lineNumber, token.columnNumber).setEndMethod(CodeBlockResult.END_METHOD_BREAK).setReturnVariable(context.getValue(tokens));
                }
                else {
                    rioaExpression = new Expression();
                    Token nextToken = tokens[i + 1];
                    if (nextToken.is('(', TokenType.SYMBOL)) {
                        ArrayList<Variable> parameters = new ArrayList<>();
                        ArrayList<Token> valueTokens = new ArrayList<>();
                        int layer = 0;
                        int j = i + 2;
                        boolean terminated = false;
                        for (; j < tokens.length; j++) {
                            Token tok = tokens[j];
                            if (tok.is('(', TokenType.SYMBOL) || tok.is('[', TokenType.SYMBOL)) layer++;
                            if (tok.is(']', TokenType.SYMBOL)) layer--;
                            if (layer == 0) {
                                if (tok.is(',', TokenType.SYMBOL) || tok.is(')', TokenType.SYMBOL)) {
                                    if (!valueTokens.isEmpty()) parameters.add(context.getValue(valueTokens));
                                    valueTokens.clear();
                                    if (tok.is(')', TokenType.SYMBOL)) {
                                        terminated = true;
                                        break;
                                    }
                                    continue;
                                }
                            }
                            else if (tok.is(')', TokenType.SYMBOL)) layer--;
                            valueTokens.add(tok);
                        }
                        if (!terminated) LangError.SYNTAX.report("Unterminated function parameters", token.lineNumber, token.columnNumber);
                        i = j;
                        context.runFunction(keyword, Arrays.asList(context.tokens).indexOf(token), parameters.toArray(new Variable[0]));
                    }
                    else {
                        String operation = "";
                        int j = i + 1;
                        for (; j < tokens.length; j++) {
                            if (tokens[j].type == TokenType.SYMBOL) operation += tokens[j].value;
                            else break;
                        }
                        i = j - 1;
                        Variable value = null;
                        if (!operation.equals("++") && !operation.equals("--")) {
                            ArrayList<Token> expressionTokens = new ArrayList<>();
                            for (j = i + 1; j < tokens.length; j++) {
                                expressionTokens.add(tokens[j]);
                                if (tokens[j].endOfLine) break;
                            }
                            if (expressionTokens.isEmpty()) LangError.SYNTAX.report("Unexpected EOL", tokens[i].lineNumber, tokens[i].columnNumber);
                            i = j;
                            value = context.getValue(expressionTokens);
                        }
                        if (operation.equals("=")) {
                            context.setVariable(keyword, value);
                        }
                        else if (operation.endsWith("=")) {
                            String op = operation.substring(0, operation.length() - 1);
                            Operator o = Operator.getOperator(op);
                            if (o == null) LangError.SYNTAX.report("Unknown operator '" + op + "'");
                            context.setVariable(keyword, o.operation.eval(context.safeGetVariable(keyword, token.lineNumber, token.columnNumber), value));
                        }
                        else if (operation.equals("++")) {
                            value = context.safeGetVariable(keyword, token.lineNumber, token.columnNumber);
                            if (value.type != VariableType.NUMBER) LangError.TYPE.report("Cannot increment variable with type " + value.type.name(), token.lineNumber, token.columnNumber);
                            context.setVariable(keyword, new Variable((double)value.value + 1, VariableType.NUMBER));
                        }
                        else if (operation.equals("--")) {
                            value = context.safeGetVariable(keyword, token.lineNumber, token.columnNumber);
                            if (value.type != VariableType.NUMBER) LangError.TYPE.report("Cannot decrement variable with type " + value.type.name(), token.lineNumber, token.columnNumber);
                            context.setVariable(keyword, new Variable((double)value.value - 1, VariableType.NUMBER));
                        }
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            LangError.SYNTAX.report("Unexpected end of code block");
        }
        catch (Exception e) {
            e.printStackTrace();
            LangError.INTERNAL.report("Unknown error occured");
        }
        return new CodeBlockResult(tokens[tokens.length - 1].lineNumber, tokens[tokens.length - 1].columnNumber);
    }
}
