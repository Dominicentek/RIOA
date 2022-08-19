package com.rioa.runtime.variable;

public class VariableArray {
    public final Variable[] array;
    public VariableArray(Variable[] array) {
        this.array = array;
    }
    public String toString() {
        return "[" + array.length + "]";
    }
    public boolean equals(Object obj) {
        if (obj instanceof VariableArray) {
            VariableArray array = (VariableArray)obj;
            for (int i = 0; i < array.array.length; i++) {
                if (!this.array[i].equals(array.array[i])) return false;
            }
            return true;
        }
        return false;
    }
}
