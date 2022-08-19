package com.rioa.expression;

import com.rioa.runtime.variable.Variable;
import com.rioa.runtime.variable.VariableType;

public enum Modifier {
    NEGATE("-", variable -> {
        if (variable.type == VariableType.NUMBER) {
            return new Variable(-(double)(variable.value), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_INVERSE("~", variable -> {
        if (variable.type == VariableType.NUMBER) {
            return new Variable((double)(~(long)((double)variable.value)), VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    BITWISE_REVERSE("@", variable -> {
        if (variable.type == VariableType.NUMBER) {
            long num = (long)((double)variable.value);
            long reversed = 0;
            for (int i = 0; i < 64; i++) {
                reversed |= (((num >> i) & 1) << (63 - i));
            }
            return new Variable((double)reversed, VariableType.NUMBER);
        }
        throw new IncompatibleTypeException();
    }),
    CONDITIONAL_INVERSE("!", variable -> {
        if (variable.type == VariableType.BOOLEAN) {
            return new Variable(!(boolean)variable.value, VariableType.BOOLEAN);
        }
        throw new IncompatibleTypeException();
    });
    public final String modifier;
    public final Modification modification;
    Modifier(String modifier, Modification modification) {
        this.modifier = modifier;
        this.modification = modification;
    }
    public static Modifier getModifier(String modifier) {
        for (Modifier mod : values()) {
            if (mod.modifier.equals(modifier)) return mod;
        }
        return null;
    }
    public String toString() {
        return modifier;
    }
}
