package com.rioa.runtime.function;

import com.rioa.runtime.variable.VariableType;
import java.util.ArrayList;

public class FunctionParameter {
    public ArrayList<VariableType<?>> types;
    public String name;
    public FunctionParameter(String name, ArrayList<VariableType<?>> types) {
        this.name = name;
        this.types = types;
    }
}
