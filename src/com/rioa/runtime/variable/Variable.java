package com.rioa.runtime.variable;

import com.rioa.expression.ExpressionUnit;

public class Variable implements ExpressionUnit {
    public Object value;
    public VariableType<?> type;
    public <T> Variable(T value, VariableType<T> type) {
        this.value = value;
        this.type = type;
    }
    public Variable() {
        this(0.0, VariableType.NUMBER);
    }
    public String toString() {
        return type.name() + " : " + value;
    }
    public Object value() {
        return value;
    }
    public VariableType<?> type() {
        return type;
    }
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable variable = (Variable)obj;
            if (type != variable.type) return false;
            return value.equals(variable.value);
        }
        return false;
    }
}
