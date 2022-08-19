package com.rioa.expression;

import com.rioa.runtime.variable.Variable;
import com.rioa.runtime.variable.VariableType;

public enum Operator implements ExpressionUnit {
    PLUS("+", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value + (double)value2.value, VariableType.NUMBER);
        }
        if (value1.type == VariableType.STRING || value2.type == VariableType.STRING) {
            return new Variable(value1.value.toString() + value2.value, VariableType.STRING);
        }
        throw new IncompatibleTypeException();
    }),
    MINUS("-", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value - (double)value2.value, VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    MULTIPLY("*", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value * (double)value2.value, VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    DIVIDE("/", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value / (double)value2.value, VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    MODULO("%", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value % (double)value2.value, VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    POWER("**", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable(Math.pow((double)value1.value, (double)value2.value), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_SHIFT_RIGHT(">>", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)((long)((double)value1.value) >> (long)((double)value2.value)), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_SHIFT_LEFT("<<", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)((long)((double)value1.value) << (long)((double)value2.value)), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_AND("&", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)((long)((double)value1.value) & (long)((double)value2.value)), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_OR("|", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)((long)((double)value1.value) | (long)((double)value2.value)), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_XOR("^", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)((long)((double)value1.value) ^ (long)((double)value2.value)), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_AND("&&", (value1, value2) -> {
        if (value1.type == VariableType.BOOLEAN && value2.type == VariableType.BOOLEAN) {
            return new Variable((boolean)value1.value && (boolean)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_OR("||", (value1, value2) -> {
        if (value1.type == VariableType.BOOLEAN && value2.type == VariableType.BOOLEAN) {
            return new Variable((boolean)value1.value || (boolean)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_XOR("^^", (value1, value2) -> {
        if (value1.type == VariableType.BOOLEAN && value2.type == VariableType.BOOLEAN) {
            return new Variable((boolean)value1.value != (boolean)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_EQUALS("==", (value1, value2) -> {
        return new Variable(value1.equals(value2), VariableType.BOOLEAN);
    }),
    CONDITIONAL_NOT_EQUALS("!=", (value1, value2) -> {
        return new Variable(!value1.equals(value2), VariableType.BOOLEAN);
    }),
    CONDITIONAL_LESS_THAN("<", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value < (double)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_GREATER_THAN(">", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value > (double)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_LESS_THAN_OR_EQUAL_TO("<=", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value <= (double)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_GREATER_THAN_OR_EQUAL_TO(">=", (value1, value2) -> {
        if (value1.type == VariableType.NUMBER && value2.type == VariableType.NUMBER) {
            return new Variable((double)value1.value >= (double)value2.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    }),
    DEBUG("?", (value1, value2) -> {
        System.out.println(value1);
        System.out.println(value2);
        return value1;
    });
    public final Operation operation;
    public final String operator;
    Operator(String operator, Operation operation) {
        this.operator = operator;
        this.operation = operation;
    }
    public static Operator getOperator(String operator) {
        for (Operator op : values()) {
            if (op.operator.equals(operator)) return op;
        }
        return null;
    }
    public String toString() {
        return operator;
    }
}
