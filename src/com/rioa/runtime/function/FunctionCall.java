package com.rioa.runtime.function;

import com.rioa.runtime.variable.Variable;
import java.util.HashMap;
import java.util.Stack;

public class FunctionCall {
    public Stack<HashMap<String, Variable>> scope = new Stack<>();
    public int callTokenIndex = 0;
}
