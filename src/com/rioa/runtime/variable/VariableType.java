package com.rioa.runtime.variable;

import java.util.ArrayList;

public class VariableType<T> {
    public static final VariableType<String> STRING = new VariableType<>("string");
    public static final VariableType<Double> NUMBER = new VariableType<>("number");
    public static final VariableType<VariableArray> ARRAY = new VariableType<>("array");
    public static final VariableType<Boolean> BOOLEAN = new VariableType<>("boolean");
    private final String name;
    private static ArrayList<VariableType<?>> values;
    private VariableType(String name) {
        this.name = name;
        if (values == null) values = new ArrayList<>();
        values.add(this);
    }
    public String name() {
        return name;
    }
    public static VariableType<?> fromName(String name) {
        for (VariableType<?> value : values) {
            if (value.name.equals(name)) return value;
        }
        return null;
    }
}
