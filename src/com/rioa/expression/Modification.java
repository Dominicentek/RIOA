package com.rioa.expression;

import com.rioa.runtime.variable.Variable;

public interface Modification {
    Variable modify(Variable variable) throws IncompatibleTypeException;
}
