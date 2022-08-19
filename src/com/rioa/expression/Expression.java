package com.rioa.expression;

import com.rioa.runtime.variable.Variable;
import com.rioa.runtime.variable.VariableType;
import java.util.ArrayList;
import java.util.Iterator;

public class Expression extends Variable implements Iterable<ExpressionUnit> {
    private final ArrayList<ExpressionUnit> units = new ArrayList<>();
    private boolean calculated = false;
    public Expression addUnit(ExpressionUnit unit) throws IncompatibleTypeException {
        return addUnit(unit, null);
    }
    public Expression addUnit(ExpressionUnit unit, Modifier modifier) throws IncompatibleTypeException {
        if (shouldAddValue() && unit instanceof Operator) throw new ExpressionException("Expected value, got operator");
        if (shouldAddOperator() && unit instanceof Variable) throw new ExpressionException("Expected operator, got value");
        if (unit instanceof Expression) {
            Expression expression = unit.asExpression();
            if (!expression.calculated) expression.calculate();
        }
        if (unit instanceof Variable) {
            if (modifier != null) unit = modifier.modification.modify(unit.asVariable());
        }
        units.add(unit);
        return this;
    }
    public Variable calculate() throws IncompatibleTypeException {
        if (units.size() == 0) throw new ExpressionException("Empty expression");
        if (units.size() % 2 == 0) throw new ExpressionException("Malformed expression");
        Variable value = units.get(0).asVariable();
        for (int i = 2; i < units.size(); i += 2) {
            Operator operator = units.get(i - 1).asOperator();
            Variable var = units.get(i).asVariable();
            value = operator.operation.eval(value, var);
        }
        this.value = value.value;
        this.type = value.type;
        return this;
    }
    public Iterator<ExpressionUnit> iterator() {
        return units.iterator();
    }
    public boolean shouldAddValue() {
        return units.size() % 2 == 0;
    }
    public boolean shouldAddOperator() {
        return units.size() % 2 == 1;
    }
    public ExpressionUnit getLastUnit() {
        return units.get(units.size() - 1);
    }
    public void setLastUnit(Variable variable) {
        units.set(units.size() - 1, variable);
    }
    public boolean isEmpty() {
        return units.isEmpty();
    }
}
