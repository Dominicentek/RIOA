package com.rioa.expression;

import com.rioa.runtime.variable.Variable;

public interface Operation {
    Variable eval(Variable value1, Variable value2) throws IncompatibleTypeException;
}
