package com.rioa.expression;

import com.rioa.runtime.variable.Variable;

public interface ExpressionUnit {
    default Operator asOperator() {
        return (Operator)this;
    }
    default Variable asVariable() {
        return (Variable)this;
    }
    default Expression asExpression() {
        return (Expression)this;
    }
}
