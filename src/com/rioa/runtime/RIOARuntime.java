package com.rioa.runtime;

import com.rioa.Debugger;
import com.rioa.expression.*;
import com.rioa.runtime.codeblock.CatchCodeBlock;
import com.rioa.runtime.error.ErrorCallback;
import com.rioa.runtime.error.LangError;
import com.rioa.runtime.function.BuiltInFunction;
import com.rioa.runtime.function.Function;
import com.rioa.runtime.function.FunctionCall;
import com.rioa.runtime.function.FunctionParameter;
import com.rioa.runtime.variable.Variable;
import com.rioa.runtime.variable.VariableArray;
import com.rioa.runtime.variable.VariableType;
import com.rioa.token.Token;
import com.rioa.token.TokenType;
import com.rioa.token.Tokenizer;
import jdk.jfr.internal.StringPool;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RIOARuntime {
    public static final String KW_IMPORT = "import";
    public static final String KW_FUNC = "func";
    public static final String KW_IF = "if";
    public static final String KW_ELSE = "else";
    public static final String KW_AND = "and";
    public static final String KW_OR = "or";
    public static final String KW_XOR = "xor";
    public static final String KW_RUN = "run";
    public static final String KW_RETURN = "return";
    public static final String KW_WHILE = "while";
    public static final String KW_TRY = "try";
    public static final String KW_CATCH = "catch";
    public static final String KW_CONTINUE = "continue";
    public static final String KW_BREAK = "break";
    public static final String KW_FALSE = "false";
    public static final String KW_TRUE = "true";
    public Token[] tokens;
    public HashMap<String, Variable> globalVariables = new HashMap<>();
    public Stack<FunctionCall> callStack = new Stack<>();
    public Stack<ErrorCallback> errorCallbackStack = new Stack<>();
    public HashMap<String, Function> functions = new HashMap<>();
    public Scanner scanner = new Scanner(System.in);
    public JFrame window = null;
    public BufferedImage graphics = null;
    public Graphics g = null;
    public ArrayList<Integer> pressedKeys = new ArrayList<>();
    public boolean leftPressed = false;
    public boolean rightPressed = false;
    public boolean middlePressed = false;
    public int scroll = 0;
    public int mouseX = 0;
    public int mouseY = 0;
    public static BufferedImage icon;
    static {
        try {
            icon = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAHKSURBVHhe7ZaLjoQwCEV1//+fd4dOcShSHs6YrM49iVHbAhdCqwsAAAAAAAAAAAAA+DLWfr8Sv/3OWDnoNYSZ6yUL8KA9rGuTr3Nok7yG6OuIXb4//X4rVIGGYmh2FRHMrZ6Q7afWZLF8aXvdIa0Ak25xgw+O+JkQLbWN62qLgBk/L4c+my9CxGDapIyn2A24W4AdaIdSRAZpR89H/RBsO0P6FM9kZBp6BRgMosAOhw0NQl9SZ0Zz+RAkp28U42wsYa7YagHImbwuj1cAa4NWN219k/tEmsqa3Q6YHCgDvB1oni75fgaRpmhe47XxzFrbRFFofWZNhkhTVjMAADTuejhYh6GZ620LID+B/dNs5lr+Fb4K2d/1aJX3/SZbb57IqYi/35U421ruAq8DPIHN2vqbktWVQYy2I7wYzMN0jKPst/kgTpuY+NoWSszBzhCU8cYIKdALrBgUG/ZtQMf2tGR1VM4Ay8EwJoUU2JLjyyDlWCWc4pRDkARUREjhB4t4mDMKQBnI619TKYDVm9mxd/iUP9OPWwBuR70/ZZvyGCGfK+g48r3CET+vTGw8BWQ7m4/8aqJMvVhMdg0AAAAAAAAAAAAA+GKW5Q+mw+8ZNlqQyQAAAABJRU5ErkJggg==")));
        }
        catch (Exception e) {
            e.printStackTrace();
            if (icon == null) LangError.INTERNAL.report("Failed to load Base64 encoded window icon image");
        }
    }
    public RIOARuntime(Token[] tokens) {
        this.tokens = tokens;
        functions.put("print", new BuiltInFunction(new FunctionParameter("value", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                System.out.print(context.getVariable("value").value);
                return new Variable();
            }
        });
        functions.put("println", new BuiltInFunction(new FunctionParameter("value", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                System.out.println(context.getVariable("value").value);
                return new Variable();
            }
        });
        functions.put("input", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                return new Variable(scanner.nextLine(), VariableType.STRING);
            }
        });
        functions.put("exit", new BuiltInFunction(new FunctionParameter("exitcode", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("exitcode");
                System.exit((int)((double)param1.value));
                return new Variable();
            }
        });
        functions.put("wait", new BuiltInFunction(new FunctionParameter("duration", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("duration");
                try {
                    Thread.sleep(Math.max(0, (long)((double)param1.value)));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    report(context, LangError.INTERNAL,"Unexpected error occured while waiting");
                }
                return new Variable();
            }
        });
        functions.put("waitnano", new BuiltInFunction(new FunctionParameter("duration", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("duration");
                try {
                    long nano = System.nanoTime();
                    while (System.nanoTime() - nano < (long)((double)param1.value));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    report(context, LangError.INTERNAL,"Unexpected error occured while waiting");
                }
                return new Variable();
            }
        });
        functions.put("typeof", new BuiltInFunction(new FunctionParameter("value", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                return new Variable(context.getVariable("value").type.name(), VariableType.STRING);
            }
        });
        functions.put("parsenum", new BuiltInFunction(new FunctionParameter("value", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("value");
                try {
                    return new Variable(Double.parseDouble((String)param1.value), VariableType.NUMBER);
                }
                catch (NumberFormatException e) {
                    report(context, LangError.TYPE,"Malformed number");
                }
                return new Variable();
            }
        });
        functions.put("includes", new BuiltInFunction(new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("regex", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                Variable param2 = context.getVariable("regex");
                return new Variable(((String)param1.value).split((String)param2.value).length > 1, VariableType.BOOLEAN);
            }
        });
        functions.put("contains", new BuiltInFunction(new FunctionParameter("array", new ArrayList<>(Arrays.asList(VariableType.ARRAY))), new FunctionParameter("content", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                Variable param2 = context.getVariable("regex");
                return new Variable(((String)param1.value).split((String)param2.value).length > 1, VariableType.BOOLEAN);
            }
        });
        functions.put("split", new BuiltInFunction(new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("regex", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                Variable param2 = context.getVariable("regex");
                String[] splitted = ((String)param1.value).split((String)param2.value);
                Variable[] array = new Variable[splitted.length];
                for (int i = 0; i < splitted.length; i++) {
                    array[i] = new Variable(splitted[i], VariableType.STRING);
                }
                return new Variable(new VariableArray(array), VariableType.ARRAY);
            }
        });
        functions.put("join", new BuiltInFunction(new FunctionParameter("stringarr", new ArrayList<>(Arrays.asList(VariableType.ARRAY))), new FunctionParameter("delimiter", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("stringarr");
                Variable param2 = context.getVariable("delimiter");
                Variable[] array = ((VariableArray)param1.value).array;
                String[] splitted = new String[array.length];
                for (int i = 0; i < array.length; i++) {
                    if (array[i].type != VariableType.STRING) report(context, LangError.TYPE, "Expected string array, found " + array[i].type.name() + " on index " + i);
                    splitted[i] = (String)array[i].value;
                }
                return new Variable(String.join((String)param2.value, splitted), VariableType.STRING);
            }
        });
        functions.put("replace", new BuiltInFunction(new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("regex", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("replacement", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                Variable param2 = context.getVariable("regex");
                Variable param3 = context.getVariable("replacement");
                return new Variable(((String)param1.value).replaceAll((String)param2.value, (String)param3.value), VariableType.STRING);
            }
        });
        functions.put("sub", new BuiltInFunction(new FunctionParameter("value", new ArrayList<>(Arrays.asList(VariableType.STRING, VariableType.ARRAY))), new FunctionParameter("beginindex", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("endindex", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("value");
                Variable param2 = context.getVariable("beginindex");
                Variable param3 = context.getVariable("endindex");
                if (param1.type == VariableType.ARRAY) {
                    Variable[] array = ((VariableArray)param1.value).array;
                    int begin = (int)((double)param2.value);
                    int end = (int)((double)param3.value);
                    if (begin > end) report(context, LangError.RANGE, "Begin index should not be greater than end index");
                    Variable[] subarray = new Variable[end - begin];
                    for (int i = begin; i < end; i++) {
                        if (i < 0 || i >= array.length) report(context, LangError.RANGE, "Array index " + i + " out of bounds of array with length " + array.length);
                        subarray[i - begin] = array[i];
                    }
                    return new Variable(new VariableArray(subarray), VariableType.ARRAY);
                }
                else {
                    String string = (String)param1.value;
                    int begin = (int)((double)param2.value);
                    int end = (int)((double)param3.value);
                    if (begin > end) report(context, LangError.RANGE, "Begin index should not be greater than end index");
                    try {
                        return new Variable(string.substring(begin, end), VariableType.STRING);
                    }
                    catch (StringIndexOutOfBoundsException e) {
                        report(context, LangError.RANGE, e.getMessage());
                    }
                }
                return null;
            }
        });
        functions.put("bytestostring", new BuiltInFunction(new FunctionParameter("bytearray", new ArrayList<>(Arrays.asList(VariableType.ARRAY)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("bytearray");
                Variable[] array = ((VariableArray)param1.value).array;
                byte[] bytearr = new byte[array.length];
                for (int i = 0; i < array.length; i++) {
                    Variable var = array[i];
                    if (var.type == VariableType.NUMBER) bytearr[i] = (byte)((double)var.value);
                    else report(context, LangError.TYPE, "Expected numeric array, found " + var.type.name() + " on index " + i);
                }
                return new Variable(new String(bytearr), VariableType.STRING);
            }
        });
        functions.put("stringtobytes", new BuiltInFunction(new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                String string = (String)param1.value;
                byte[] data = string.getBytes();
                Variable[] bytes = new Variable[data.length];
                for (int i = 0; i < data.length; i++) {
                    bytes[i] = new Variable((double)Byte.toUnsignedInt(data[i]), VariableType.NUMBER);
                }
                return new Variable(new VariableArray(bytes), VariableType.ARRAY);
            }
        });
        functions.put("tocharcode", new BuiltInFunction(new FunctionParameter("character", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("character");
                String character = (String)param1.value;
                if (character.length() != 1) report(context, LangError.RANGE, "Expected string with 1 character, found string with " + character.length() + " characters");
                return new Variable((double)((int)character.charAt(0)), VariableType.NUMBER);
            }
        });
        functions.put("fromcharcode", new BuiltInFunction(new FunctionParameter("charcode", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("charcode");
                char character = (char)((double)param1.value);
                return new Variable(character + "", VariableType.STRING);
            }
        });
        functions.put("lowercase", new BuiltInFunction(new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                String string = (String)param1.value;
                return new Variable(string.toLowerCase(), VariableType.STRING);
            }
        });
        functions.put("uppercase", new BuiltInFunction(new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("string");
                String string = (String)param1.value;
                return new Variable(string.toUpperCase(), VariableType.STRING);
            }
        });
        functions.put("gfxinit", new BuiltInFunction(new FunctionParameter("title", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("width", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("height", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                if (g != null) report(context, LangError.GRAPHICS, "Graphics already initialized");
                Variable param1 = context.getVariable("title");
                Variable param2 = context.getVariable("width");
                Variable param3 = context.getVariable("height");
                String title = (String)param1.value;
                int width = (int)((double)param2.value);
                int height = (int)((double)param3.value);
                graphics = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                g = graphics.getGraphics();
                g.setColor(new Color(0xFFFFFFFF));
                window = new JFrame(title);
                window.setLocation(100, 100);
                window.getContentPane().setPreferredSize(new Dimension(width, height));
                window.pack();
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                window.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (!pressedKeys.contains(e.getKeyCode())) pressedKeys.add(e.getKeyCode());
                    }
                    public void keyReleased(KeyEvent e) {
                        pressedKeys.remove((Integer)e.getKeyCode());
                    }
                });
                window.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) leftPressed = true;
                        if (e.getButton() == MouseEvent.BUTTON2) middlePressed = true;
                        if (e.getButton() == MouseEvent.BUTTON3) rightPressed = true;
                    }
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) leftPressed = false;
                        if (e.getButton() == MouseEvent.BUTTON2) middlePressed = false;
                        if (e.getButton() == MouseEvent.BUTTON3) rightPressed = false;
                    }
                });
                window.addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        mouseMoved(e);
                    }
                    public void mouseMoved(MouseEvent e) {
                        mouseX = e.getXOnScreen() - window.getContentPane().getLocationOnScreen().x;
                        mouseY = e.getYOnScreen() - window.getContentPane().getLocationOnScreen().y;
                    }
                });
                window.addMouseWheelListener(e -> {
                    scroll += e.getScrollAmount();
                });
                window.add(new JPanel() {
                    public void paint(Graphics g) {
                        g.drawImage(graphics, 0, 0, window.getContentPane().getWidth(), window.getContentPane().getHeight(), this);
                    }
                });
                window.setIconImage(icon);
                window.setResizable(false);
                window.setVisible(true);
                return new Variable();
            }
        });
        functions.put("gfxcolor", new BuiltInFunction(new FunctionParameter("r", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("g", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("b", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("a", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("r");
                Variable param2 = context.getVariable("g");
                Variable param3 = context.getVariable("b");
                Variable param4 = context.getVariable("a");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                int r = Byte.toUnsignedInt((byte)((double)param1.value));
                int g = Byte.toUnsignedInt((byte)((double)param2.value));
                int b = Byte.toUnsignedInt((byte)((double)param3.value));
                int a = Byte.toUnsignedInt((byte)((double)param4.value));
                context.g.setColor(new Color(r, g, b, a));
                return new Variable();
            }
        });
        functions.put("gfxrectfill", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("width", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("height", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                Variable param3 = context.getVariable("width");
                Variable param4 = context.getVariable("height");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                g.fillRect((int)((double)param1.value), (int)((double)param2.value), (int)((double)param3.value), (int)((double)param4.value));
                window.repaint();
                return new Variable();
            }
        });
        functions.put("gfxrectdraw", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("width", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("height", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                Variable param3 = context.getVariable("width");
                Variable param4 = context.getVariable("height");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                if (param1.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param1.type.name());
                if (param2.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param2.type.name());
                if (param3.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param3.type.name());
                if (param4.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param4.type.name());
                g.drawRect((int)((double)param1.value), (int)((double)param2.value), (int)((double)param3.value), (int)((double)param4.value));
                window.repaint();
                return new Variable();
            }
        });
        functions.put("gfxcirclefill", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("radius", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                Variable param3 = context.getVariable("radius");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                if (param1.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param1.type.name());
                if (param2.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param2.type.name());
                if (param3.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param3.type.name());
                int x = (int)((double)param1.value);
                int y = (int)((double)param2.value);
                int r = (int)((double)param3.value);
                g.fillOval(x - r, y - r, r * 2, r * 2);
                window.repaint();
                return new Variable();
            }
        });
        functions.put("gfxcircledraw", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("radius", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                Variable param3 = context.getVariable("radius");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                if (param1.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param1.type.name());
                if (param2.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param2.type.name());
                if (param3.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected number, found " + param3.type.name());
                int x = (int)((double)param1.value);
                int y = (int)((double)param2.value);
                int r = (int)((double)param3.value);
                g.drawOval(x - r, y - r, r * 2, r * 2);
                window.repaint();
                return new Variable();
            }
        });
        functions.put("gfxfont", new BuiltInFunction(new FunctionParameter("fontname", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("fontsize", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("fontname");
                Variable param2 = context.getVariable("fontsize");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                g.setFont(new Font((String)param1.value, Font.PLAIN, (int)((double)param2.value)));
                window.repaint();
                return new Variable();
            }
        });
        functions.put("gfxstring", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("string", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                Variable param3 = context.getVariable("string");
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                int x = (int)((double)param1.value);
                int y = (int)((double)param2.value);
                String string = (String)param3.value;
                g.drawString(string, x, y);
                window.repaint();
                return new Variable();
            }
        });
        functions.put("gfximagedynamic", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("image", new ArrayList<>(Arrays.asList(VariableType.STRING, VariableType.ARRAY)))) {
            public Variable run(RIOARuntime context) {
                try {
                    Variable param1 = context.getVariable("x");
                    Variable param2 = context.getVariable("y");
                    Variable param5 = context.getVariable("image");
                    if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                    if (param5.type == VariableType.ARRAY) {
                        Variable[] arr = ((VariableArray)param5.value).array;
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected numeric array, found " + arr[i].type.name() + " on index " + i);
                        }
                    }
                    BufferedImage image;
                    if (param5.type == VariableType.ARRAY) {
                        Variable[] arr = ((VariableArray)param5.value).array;
                        int width = (int)((double)arr[0].value);
                        int height = arr.length / width;
                        image = new BufferedImage(width, arr.length / width, BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                image.setRGB(x, y, (int)((double)arr[y * height + x].value));
                            }
                        }
                    }
                    else image = ImageIO.read(new FileInputStream((String)param5.value));
                    int x = (int)((double)param1.value);
                    int y = (int)((double)param2.value);
                    g.drawImage(image, x, y, null);
                    window.repaint();
                    return new Variable();
                }
                catch (IOException e) {
                    report(context, LangError.IO, e.getMessage());
                    return null;
                }
            }
        });
        functions.put("gfximagefixed", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("width", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("height", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("image", new ArrayList<>(Arrays.asList(VariableType.STRING, VariableType.ARRAY)))) {
            public Variable run(RIOARuntime context) {
                try {
                    Variable param1 = context.getVariable("x");
                    Variable param2 = context.getVariable("y");
                    Variable param3 = context.getVariable("width");
                    Variable param4 = context.getVariable("height");
                    Variable param5 = context.getVariable("image");
                    if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                    if (param5.type == VariableType.ARRAY) {
                        Variable[] arr = ((VariableArray)param5.value).array;
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected numeric array, found " + arr[i].type.name() + " on index " + i);
                        }
                    }
                    BufferedImage image;
                    if (param5.type == VariableType.ARRAY) {
                        Variable[] arr = ((VariableArray)param5.value).array;
                        int width = (int)((double)arr[0].value);
                        int height = arr.length / width;
                        image = new BufferedImage(width, arr.length / width, BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                image.setRGB(x, y, (int)((double)arr[y * height + x].value));
                            }
                        }
                    }
                    else image = ImageIO.read(new FileInputStream((String)param5.value));
                    int x = (int)((double)param1.value);
                    int y = (int)((double)param2.value);
                    int w = (int)((double)param3.value);
                    int h = (int)((double)param4.value);
                    g.drawImage(image, x, y, w, h, null);
                    window.repaint();
                    return new Variable();
                }
                catch (IOException e) {
                    report(context, LangError.IO, e.getMessage());
                    return null;
                }
            }
        });
        functions.put("gfxline", new BuiltInFunction(new FunctionParameter("x1", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y1", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("x2", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y2", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                Variable param1 = context.getVariable("x1");
                Variable param2 = context.getVariable("y1");
                Variable param3 = context.getVariable("x2");
                Variable param4 = context.getVariable("y2");
                g.drawLine((int)((double)param1.value), (int)((double)param2.value), (int)((double)param3.value), (int)((double)param4.value));
                window.repaint();
                return new Variable();
            }
        });
        functions.put("keypressed", new BuiltInFunction(new FunctionParameter("keycode", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                Variable param1 = context.getVariable("keycode");
                if (param1.type != VariableType.NUMBER) return new Variable();
                int keycode = (int)((double)param1.value);
                return new Variable(pressedKeys.contains(keycode), VariableType.BOOLEAN);
            }
        });
        functions.put("mouseposx", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                return new Variable((double)mouseX, VariableType.NUMBER);
            }
        });
        functions.put("mouseposy", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                return new Variable((double)mouseY, VariableType.NUMBER);
            }
        });
        functions.put("mouseleftpressed", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                return new Variable(leftPressed, VariableType.BOOLEAN);
            }
        });
        functions.put("mouserightpressed", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                return new Variable(rightPressed, VariableType.BOOLEAN);
            }
        });
        functions.put("mousemiddlepressed", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                return new Variable(middlePressed, VariableType.BOOLEAN);
            }
        });
        functions.put("mousescrollamount", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                int scroll = context.scroll;
                context.scroll = 0;
                return new Variable((double)scroll, VariableType.NUMBER);
            }
        });
        functions.put("closegfx", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                if (g == null) report(context, LangError.GRAPHICS, "Graphics not initialized");
                window.dispose();
                window = null;
                graphics = null;
                g.dispose();
                g = null;
                return new Variable();
            }
        });
        functions.put("audio", new BuiltInFunction(new FunctionParameter("filename", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("block", new ArrayList<>(Arrays.asList(VariableType.BOOLEAN)))) {
            public Variable run(RIOARuntime context) {
                try {
                    Variable param1 = context.getVariable("filename");
                    Variable param2 = context.getVariable("block");
                    Value<Boolean> block = new Value<>((boolean)param2.value);
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(new File((String)param1.value)));
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            block.value = false;
                            clip.flush();
                            clip.close();
                        }
                    });
                    clip.start();
                    while (block.value) {
                        Thread.sleep(100);
                    }
                    return new Variable();
                }
                catch (IOException e) {
                    report(context, LangError.IO, e.getMessage());
                }
                catch (Exception e) {
                    e.printStackTrace();
                    report(context, LangError.INTERNAL, "Internal error occured");
                }
                return null;
            }
        });
        functions.put("ioread", new BuiltInFunction(new FunctionParameter("filepath", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("filepath");
                try {
                    InputStream in = new FileInputStream((String)param1.value);
                    byte[] data = new byte[in.available()];
                    in.read(data);
                    in.close();
                    Variable[] variables = new Variable[data.length];
                    for (int i = 0; i < data.length; i++) {
                        variables[i] = new Variable((double)Byte.toUnsignedInt(data[i]), VariableType.NUMBER);
                    }
                    return new Variable(new VariableArray(variables), VariableType.ARRAY);
                }
                catch (IOException e) {
                    report(context, LangError.IO, e.getMessage());
                }
                return null;
            }
        });
        functions.put("iowrite", new BuiltInFunction(new FunctionParameter("filepath", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("data", new ArrayList<>(Arrays.asList(VariableType.ARRAY)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("filepath");
                Variable param2 = context.getVariable("data");
                try {
                    Variable[] arr = ((VariableArray)param2.value).array;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected numeric array, found " + arr[i].type.name() + " on index " + i);
                    }
                    Variable[] varArr = ((VariableArray)param2.value).array;
                    byte[] array = new byte[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        array[i] = (byte)((double)varArr[i].value);
                    }
                    OutputStream out = new FileOutputStream((String)param1.value);
                    out.write(array);
                    out.close();
                }
                catch (IOException e) {
                    report(context, LangError.IO, e.getMessage());
                }
                return new Variable();
            }
        });
        functions.put("iofolder", new BuiltInFunction(new FunctionParameter("foldername", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("foldername");
                new File((String)param1.value).mkdirs();
                return new Variable();
            }
        });
        functions.put("iodelete", new BuiltInFunction(new FunctionParameter("iodelete", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("filename");
                delete(new File((String)param1.value));
                return new Variable();
            }
        });
        functions.put("iolistdir", new BuiltInFunction(new FunctionParameter("iodelete", new ArrayList<>(Arrays.asList(VariableType.STRING)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("filename");
                File file = new File((String)param1.value);
                if (file.isDirectory()) {
                    String[] filenames = file.list();
                    Variable[] array = new Variable[filenames.length];
                    for (int i = 0; i < array.length; i++) {
                        array[i] = new Variable(filenames[i], VariableType.STRING);
                    }
                    return new Variable(new VariableArray(array), VariableType.ARRAY);
                }
                else report(context, LangError.IO, "Trying to list files inside of a file");
                return new Variable();
            }
        });
        functions.put("pi", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                return new Variable(Math.PI, VariableType.NUMBER);
            }
        });
        functions.put("e", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                return new Variable(Math.E, VariableType.NUMBER);
            }
        });
        functions.put("abs", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.abs((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("acos", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.acos((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("asin", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.asin((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("atan", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.atan((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("atan2", new BuiltInFunction(new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("y");
                Variable param2 = context.getVariable("x");
                return new Variable(Math.atan2((double)param1.value, (double)param2.value), VariableType.NUMBER);
            }
        });
        functions.put("cb", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.pow((double)param1.value, 3), VariableType.NUMBER);
            }
        });
        functions.put("cbrt", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.cbrt((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("ceil", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.cbrt((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("clamp", new BuiltInFunction(new FunctionParameter("a", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("b", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("c", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("a");
                Variable param2 = context.getVariable("b");
                Variable param3 = context.getVariable("c");
                double a = (double)param1.value;
                double b = (double)param2.value;
                double c = (double)param3.value;
                return new Variable(Math.min(Math.max(a, b), c), VariableType.NUMBER);
            }
        });
        functions.put("cos", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.cos((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("cosh", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.cosh((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("deg", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.toDegrees((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("exp", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.exp((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("floor", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.floor((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("hypot", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                return new Variable(Math.hypot((double)param1.value, (double)param2.value), VariableType.NUMBER);
            }
        });
        functions.put("lerp", new BuiltInFunction(new FunctionParameter("a", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("b", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("c", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("a");
                Variable param2 = context.getVariable("b");
                Variable param3 = context.getVariable("c");
                double a = (double)param1.value;
                double b = (double)param2.value;
                double c = (double)param3.value;
                return new Variable(b + a * (c - b), VariableType.NUMBER);
            }
        });
        functions.put("log", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.log((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("log10", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.log10((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("max", new BuiltInFunction(new FunctionParameter("array", new ArrayList<>(Arrays.asList(VariableType.ARRAY)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("array");
                Variable[] array = ((VariableArray)param1.value).array;
                int i = 0;
                for (Variable variable : array) {
                    if (variable.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected numeric array, found " + variable.type.name() + " on index " + i);
                    i++;
                }
                double max = -Double.MAX_VALUE;
                for (Variable variable : array) {
                    max = Math.max(max, (double)variable.value);
                }
                return new Variable(max, VariableType.NUMBER);
            }
        });
        functions.put("min", new BuiltInFunction(new FunctionParameter("array", new ArrayList<>(Arrays.asList(VariableType.ARRAY)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("array");
                Variable[] array = ((VariableArray)param1.value).array;
                int i = 0;
                for (Variable variable : array) {
                    if (variable.type != VariableType.NUMBER) report(context, LangError.TYPE, "Expected numeric array, found " + variable.type.name() + " on index " + i);
                    i++;
                }
                double min = Double.MAX_VALUE;
                for (Variable variable : array) {
                    min = Math.min(min, (double)variable.value);
                }
                return new Variable(min, VariableType.NUMBER);
            }
        });
        functions.put("pow", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                return new Variable(Math.pow((double)param1.value, (double)param2.value), VariableType.NUMBER);
            }
        });
        functions.put("rad", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.toRadians((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("random", new BuiltInFunction() {
            public Variable run(RIOARuntime context) {
                return new Variable(Math.random(), VariableType.NUMBER);
            }
        });
        functions.put("round", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable((double)Math.round((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("rt", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                return new Variable(Math.pow((double)param1.value, 1 / (double)param2.value), VariableType.NUMBER);
            }
        });
        functions.put("scalb", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER))), new FunctionParameter("y", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                Variable param2 = context.getVariable("y");
                return new Variable(Math.scalb((double)param1.value, (int)((double)param2.value)), VariableType.NUMBER);
            }
        });
        functions.put("signum", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.signum((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("sin", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.sin((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("sinh", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.sinh((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("sq", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.pow((double)param1.value, 2), VariableType.NUMBER);
            }
        });
        functions.put("tan", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.tan((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("tanh", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.tanh((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("ulp", new BuiltInFunction(new FunctionParameter("x", new ArrayList<>(Arrays.asList(VariableType.NUMBER)))) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("x");
                return new Variable(Math.ulp((double)param1.value), VariableType.NUMBER);
            }
        });
        functions.put("syntaxerr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.SYNTAX, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("deferr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.DEFINE, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("rangeerr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.RANGE, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("typeerr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.TYPE, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("ioerr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.IO, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("gfxerr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.GRAPHICS, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("internalerr", new BuiltInFunction(new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                report(context, LangError.INTERNAL, context.getVariable("msg").value.toString());
                return null;
            }
        });
        functions.put("customerr", new BuiltInFunction(new FunctionParameter("name", new ArrayList<>(Arrays.asList(VariableType.STRING))), new FunctionParameter("msg", new ArrayList<>())) {
            public Variable run(RIOARuntime context) {
                Variable param1 = context.getVariable("name");
                report(context, new LangError((String)param1.value), context.getVariable("msg").value.toString());
                return null;
            }
        });
    }
    public void analyzeTokens() {
        Debugger.start("Token analysis");
        Token token = null;
        try {
            boolean canImport = true;
            for (int i = 0; i < tokens.length; i++) {
                token = tokens[i];
                if (token.is(KW_IMPORT, TokenType.WORD)) {
                    if (!canImport) LangError.SYNTAX.report(this, "Imports should be defined at the top of the file", token.lineNumber, token.columnNumber);
                    Token importToken = tokens[i + 1];
                    if (importToken.type != TokenType.STRING_LITERAL) LangError.SYNTAX.report(this, "Expected string literal", importToken.lineNumber, importToken.columnNumber);
                    if (!importToken.endOfLine) LangError.SYNTAX.report(this, "Expected EOL", importToken.lineNumber, importToken.columnNumber);
                    String code = "";
                    try {
                        InputStream in = new FileInputStream((String)importToken.value);
                        byte[] data = new byte[in.available()];
                        in.read(data);
                        in.close();
                        code = new String(data);
                    }
                    catch (Exception e) {
                        LangError.IO.report(this, e.getMessage());
                    }
                    i++;
                    importCode(code);
                }
                else {
                    canImport = false;
                    if (token.is(KW_FUNC, TokenType.WORD)) {
                        Token name = tokens[i + 1];
                        if (functions.get(name) != null) LangError.DEFINE.report(this, "Function " + name + " is already defined", name.lineNumber, name.columnNumber);
                        if (name.type != TokenType.WORD) LangError.SYNTAX.report(this, "Word expected", name.lineNumber, name.columnNumber);
                        ArrayList<FunctionParameter> params = new ArrayList<>();
                        if (!tokens[i + 2].is('(', TokenType.SYMBOL)) LangError.SYNTAX.report(this, "Parameter initialization expected", tokens[i + 2].lineNumber, tokens[i + 2].columnNumber);
                        boolean terminated = false;
                        int j = i + 3;
                        ArrayList<String> words = new ArrayList<>();
                        for (; j < tokens.length; j++) {
                            Token tok = tokens[j];
                            if (tok.is(')', TokenType.SYMBOL) || tok.is(',', TokenType.SYMBOL)) {
                                if (words.isEmpty() && !(tok.is(')', TokenType.SYMBOL) && params.isEmpty())) LangError.SYNTAX.report(this, "Empty parameter", tok.lineNumber, tok.columnNumber);
                                ArrayList<VariableType<?>> types = new ArrayList<>();
                                for (int k = 0; k < words.size() - 1; k++) {
                                    VariableType<?> type = VariableType.fromName(words.get(k));
                                    if (type == null) LangError.TYPE.report(this, "Variable type " + words.get(k) + " doesn't exist", tok.lineNumber, tok.columnNumber);
                                    types.add(type);
                                }
                                if (!words.isEmpty()) params.add(new FunctionParameter(words.get(words.size() - 1), types));
                                words.clear();
                                if (tok.is(')', TokenType.SYMBOL)) {
                                    terminated = true;
                                    break;
                                }
                            }
                            else if (tok.type == TokenType.WORD) {
                                words.add((String)tok.value);
                            }
                            else LangError.SYNTAX.report(this, "Illegal token", tok.lineNumber, tok.columnNumber);
                        }
                        if (!terminated) LangError.SYNTAX.report(this, "Unterminated parameter initialization");
                        i = j + 1;
                        token = tokens[i];
                        if (!token.is('{', TokenType.SYMBOL)) LangError.SYNTAX.report(this, "Code block expected", token.lineNumber, token.columnNumber);
                        j = i + 1;
                        terminated = false;
                        ArrayList<Token> funcTokens = new ArrayList<>();
                        int l = 0;
                        for (; j < tokens.length; j++) {
                            if (tokens[j].is('{', TokenType.SYMBOL)) l++;
                            if (tokens[j].is('}', TokenType.SYMBOL)) l--;
                            if (l == -1) {
                                terminated = true;
                                break;
                            }
                            funcTokens.add(tokens[j]);
                        }
                        if (!terminated) LangError.SYNTAX.report(this, "Unterminated code block");
                        i = j;
                        functions.put((String)name.value, new Function(funcTokens.toArray(new Token[0]), params.toArray(new FunctionParameter[0])));
                    }
                    else {
                        if (token.type != TokenType.WORD) LangError.SYNTAX.report(this, "Word expected", token.lineNumber, token.columnNumber);
                        String varname = (String)token.value;
                        Token equals = tokens[i + 1];
                        if (!equals.is('=', TokenType.SYMBOL)) LangError.SYNTAX.report(this, "'=' expected", equals.lineNumber, equals.columnNumber);
                        ArrayList<Token> valueTokens = new ArrayList<>();
                        for (int j = i + 2; j < tokens.length; j++) {
                            Token tok = tokens[j];
                            valueTokens.add(tok);
                            if (tok.endOfLine) break;
                        }
                        if (valueTokens.isEmpty()) LangError.SYNTAX.report(this, "Expression expected", tokens[i + 2].lineNumber, tokens[i + 2].columnNumber);
                        i += 1 + valueTokens.size();
                        setVariable(varname, getValue(valueTokens));
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            LangError.SYNTAX.report(this, "Unexpected EOF", token.lineNumber, token.columnNumber);
        }
        Debugger.end();
    }
    public Variable run(String[] args) {
        analyzeTokens();
        Debugger.start("Running");
        Variable[] arguments = new Variable[args.length];
        for (int i = 0; i < args.length; i++) {
            arguments[i] = new Variable(args[i], VariableType.STRING);
        }
        Variable returningValue = runFunction("main", -1, new Variable(new VariableArray(arguments), VariableType.ARRAY));
        Debugger.end();
        return returningValue;
    }
    public void setVariable(String name, Variable value) {
        if (getVariable(name) == null) {
            if (value == null) LangError.INTERNAL.report(this, "Trying to undefine variable '" + name + "' when it was already undefined.");
            if (callStack.isEmpty()) globalVariables.put(name, value);
            else callStack.peek().scope.peek().put(name, value);
        }
        else {
            boolean assigned = false;
            for (String varname : globalVariables.keySet()) {
                if (varname.equals(name)) {
                    if (value == null) globalVariables.remove(varname);
                    else globalVariables.put(varname, value);
                    assigned = true;
                    break;
                }
            }
            if (callStack.isEmpty()) return;
            for (HashMap<String, Variable> scope : callStack.peek().scope) {
                for (String varname : scope.keySet()) {
                    if (varname.equals(name)) {
                        if (value == null) scope.remove(varname);
                        else scope.put(varname, value);
                        assigned = true;
                    }
                }
            }
            if (!assigned) {
                LangError.INTERNAL.report(this, "getVariable didn't return null but cannot assign new value, this shouldn't happen.");
            }
        }
    }
    public Variable getValue(ArrayList<Token> tokens) {
        if (tokens.size() == 0) LangError.SYNTAX.report(this, "Empty expression");
        int layer = 0;
        for (Token token : tokens) {
            if (token.is('(', TokenType.SYMBOL)) layer++;
            if (token.is(')', TokenType.SYMBOL)) layer--;
        }
        if (layer != 0) LangError.SYNTAX.report(this, "Malformed expression", tokens.get(0).lineNumber, tokens.get(0).columnNumber);
        try {
            Expression expression = new Expression();
            Modifier modifier = null;
            final Value<String> operatorValue = new Value<>();
            Runnable addOperator = () -> {
                if (operatorValue.value != null) {
                    Operator operator = Operator.getOperator(operatorValue.value);
                    if (operator == null) LangError.SYNTAX.report(this, "Invalid operator " + operatorValue.value, tokens.get(0).lineNumber, tokens.get(0).columnNumber);
                    operatorValue.value = null;
                    try {
                        expression.addUnit(operator);
                    }
                    catch (ExpressionException e) {
                        LangError.SYNTAX.report(this, "Malformed expression (" + e.getMessage() + ")", tokens.get(0).lineNumber, tokens.get(0).columnNumber);
                    }
                    catch (IncompatibleTypeException e) {
                        LangError.SYNTAX.report(this, "Attempted to calculate an operation with incompatible types", tokens.get(0).lineNumber, tokens.get(0).columnNumber);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        LangError.INTERNAL.report(this, e.getMessage(), tokens.get(0).lineNumber, tokens.get(0).columnNumber);
                    }
                }
            };
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (token.is('[', TokenType.SYMBOL)) {
                    if (expression.shouldAddOperator()) {
                        if (i + 1 == tokens.size()) LangError.SYNTAX.report(this, "Unexpected end of expression", token.lineNumber, token.columnNumber);
                        ArrayList<Token> expressionTokens = new ArrayList<>();
                        int l = 0;
                        for (i++; i < tokens.size(); i++) {
                            if (tokens.get(i).is('[', TokenType.SYMBOL)) l++;
                            if (tokens.get(i).is(']', TokenType.SYMBOL)) l--;
                            if (l == -1) break;
                            expressionTokens.add(tokens.get(i));
                        }
                        int index = -1;
                        if (!expressionTokens.isEmpty()) {
                            Variable value = getValue(expressionTokens);
                            if (value.type != VariableType.NUMBER) LangError.TYPE.report(this, "Expected number, found " + value.type.name(), token.lineNumber, token.columnNumber);
                            index = (int)((double)value.value);
                        }
                        Variable variable = expression.getLastUnit().asVariable();
                        if (index >= 0) {
                            if (variable.type == VariableType.STRING) {
                                String value = (String)variable.value;
                                if (value.length() <= index) LangError.RANGE.report(this, "String index out of bounds (index: " + index + "; string length: " + value.length() + ")");
                                variable = new Variable(value.charAt(index) + "", VariableType.STRING);
                            }
                            else if (variable.type == VariableType.ARRAY) {
                                VariableArray value = (VariableArray)variable.value;
                                if (value.array.length <= index) LangError.RANGE.report(this, "Array index out of bounds (index: " + index + "; array capacity: " + value.array.length + ")");
                                variable = value.array[index];
                            }
                            else LangError.SYNTAX.report(this, "Trying to get element from " + variable.type.name(), token.lineNumber, token.columnNumber);
                        }
                        else {
                            if (variable.type == VariableType.STRING) variable = new Variable((double)((String)variable.value).length(), VariableType.NUMBER);
                            else if (variable.type == VariableType.ARRAY) variable = new Variable((double)((VariableArray)variable.value).array.length, VariableType.NUMBER);
                            else LangError.SYNTAX.report(this, "Trying to get element from " + variable.type.name(), token.lineNumber, token.columnNumber);
                        }
                        expression.setLastUnit(variable);
                        continue;
                    }
                    addOperator.run();
                    int j = i + 1;
                    ArrayList<Token> elementTokens = new ArrayList<>();
                    ArrayList<Variable> variables = new ArrayList<>();
                    boolean terminated = false;
                    int l = 0;
                    for (; j < tokens.size(); j++) {
                        Token tok = tokens.get(j);
                        if (tok.is('[', TokenType.SYMBOL) || tok.is('(', TokenType.SYMBOL)) l++;
                        if (tok.is(')', TokenType.SYMBOL)) l--;
                        if (tok.is(']', TokenType.SYMBOL) || tok.is(',', TokenType.SYMBOL)) {
                            if (l == 0) {
                                if (!elementTokens.isEmpty()) variables.add(getValue(elementTokens));
                                elementTokens.clear();
                                if (tok.is(']', TokenType.SYMBOL)) {
                                    terminated = true;
                                    break;
                                }
                            }
                            else {
                                elementTokens.add(tok);
                                if (tok.is(']', TokenType.SYMBOL)) l--;
                            }
                        }
                        else {
                            elementTokens.add(tok);
                        }
                    }
                    if (!terminated) LangError.SYNTAX.report(this, "Unterminated array", tokens.get(i).lineNumber, tokens.get(i).columnNumber);
                    VariableArray array = new VariableArray(variables.toArray(new Variable[0]));
                    expression.addUnit(new Variable(array, VariableType.ARRAY));
                    i = j;
                }
                else if (token.is('(', TokenType.SYMBOL)) {
                    addOperator.run();
                    layer = 0;
                    ArrayList<Token> tkns = new ArrayList<>();
                    for (int j = i + 1; j < tokens.size(); j++) {
                        if (tokens.get(j).is('(', TokenType.SYMBOL)) layer++;
                        if (tokens.get(j).is(')', TokenType.SYMBOL)) {
                            if (layer == 0) break;
                            layer--;
                        }
                        tkns.add(tokens.get(j));
                    }
                    i += tkns.size() + 1;
                    Variable value = getValue(tkns);
                    expression.addUnit(value, modifier);
                    modifier = null;
                }
                else if (token.is(KW_TRUE, TokenType.WORD)) {
                    addOperator.run();
                    expression.addUnit(new Variable(true, VariableType.BOOLEAN), modifier);
                    modifier = null;
                }
                else if (token.is(KW_FALSE, TokenType.WORD)) {
                    addOperator.run();
                    expression.addUnit(new Variable(false, VariableType.BOOLEAN), modifier);
                    modifier = null;
                }
                else if (token.type == TokenType.WORD) {
                    addOperator.run();
                    boolean isFunction = false;
                    if (i + 1 < tokens.size()) {
                        isFunction = tokens.get(i + 1).is('(', TokenType.SYMBOL);
                    }
                    String name = (String)token.value;
                    if (isFunction) {
                        ArrayList<Token> elementTokens = new ArrayList<>();
                        ArrayList<Variable> parameters = new ArrayList<>();
                        boolean terminated = false;
                        int j = i + 2;
                        int l = 0;
                        for (; j < tokens.size(); j++) {
                            Token tok = tokens.get(j);
                            if (tok.is('(', TokenType.SYMBOL) || tok.is('[', TokenType.SYMBOL)) l++;
                            if (tok.is(']', TokenType.SYMBOL)) l--;
                            if (tok.is(')', TokenType.SYMBOL) || tok.is(',', TokenType.SYMBOL)) {
                                if (l == 0) {
                                    if (!elementTokens.isEmpty()) parameters.add(getValue(elementTokens));
                                    elementTokens.clear();
                                    if (tok.is(')', TokenType.SYMBOL)) {
                                        terminated = true;
                                        break;
                                    }
                                }
                                else {
                                    elementTokens.add(tok);
                                    if (tok.is(')', TokenType.SYMBOL)) l--;
                                }
                            }
                            else {
                                elementTokens.add(tok);
                            }
                        }
                        if (!terminated) LangError.SYNTAX.report(this, "Unterminated function parameters", tokens.get(i + 1).lineNumber, tokens.get(i + 1).columnNumber);
                        expression.addUnit(runFunction(name, Arrays.asList(this.tokens).indexOf(token), parameters.toArray(new Variable[0])));
                        modifier = null;
                        i = j;
                    }
                    else {
                        expression.addUnit(safeGetVariable(name, token.lineNumber, token.columnNumber), modifier);
                        modifier = null;
                    }
                }
                else if (token.type == TokenType.SYMBOL) {
                    char character = (char)token.value;
                    Modifier mod = Modifier.getModifier(character + "");
                    if (mod != null) {
                        boolean isModifier = true;
                        if (mod == Modifier.NEGATE || mod == Modifier.CONDITIONAL_INVERSE) {
                            isModifier = expression.shouldAddValue();
                        }
                        if (isModifier) {
                            addOperator.run();
                            modifier = mod;
                            continue;
                        }
                    }
                    if (operatorValue.value == null) operatorValue.value = "";
                    operatorValue.value += character;
                }
                else if (token.type == TokenType.NUMBER) {
                    addOperator.run();
                    double value = (long)token.value;
                    if (i + 1 < tokens.size()) {
                        if (tokens.get(i + 1).is('.', TokenType.SYMBOL) && i + 2 < tokens.size()) {
                            Token tok = tokens.get(i + 2);
                            if (tok.type == TokenType.NUMBER) {
                                long decimal = (long)tok.value;
                                if (decimal != 0) {
                                    value += (decimal / Math.pow(10, (int)(Math.log10(decimal) + 1)));
                                }
                                i += 2;
                            }
                        }
                    }
                    expression.addUnit(new Variable(value, VariableType.NUMBER), modifier);
                    modifier = null;
                }
                else if (token.type == TokenType.STRING_LITERAL) {
                    addOperator.run();
                    expression.addUnit(new Variable((String)token.value, VariableType.STRING), modifier);
                    modifier = null;
                }
                if (modifier != null) throw new ExpressionException("Modifier used in illegal place");
            }
            return expression.calculate();
        }
        catch (ExpressionException e) {
            LangError.SYNTAX.report(this, "Malformed expression (" + e.getMessage() + ")", tokens.get(0).lineNumber, tokens.get(0).columnNumber);
        }
        catch (IncompatibleTypeException e) {
            LangError.SYNTAX.report(this, "Attempted to calculate an operation with incompatible types", tokens.get(0).lineNumber, tokens.get(0).columnNumber);
        }
        catch (Exception e) {
            e.printStackTrace();
            LangError.INTERNAL.report(this, e.getMessage(), tokens.get(0).lineNumber, tokens.get(0).columnNumber);
        }
        return null;
    }
    public int getCallLineNumber() {
        return tokens[callStack.peek().callTokenIndex].lineNumber;
    }
    public int getCallColumnNumber() {
        return tokens[callStack.peek().callTokenIndex].columnNumber;
    }
    public Variable safeGetVariable(String name, int lineNumber, int columnNumber) {
        Variable variable = getVariable(name);
        if (variable == null) LangError.DEFINE.report(this, "Variable '" + name + "' is not defined", lineNumber, columnNumber);
        return variable;
    }
    public Variable getVariable(String name) {
        if (callStack.isEmpty()) {
            return globalVariables.get(name);
        }
        Stack<HashMap<String, Variable>> scopeStack = callStack.peek().scope;
        Variable variable = null;
        for (String globalVariable : globalVariables.keySet()) {
            if (name.equals(globalVariable)) {
                variable = globalVariables.get(globalVariable);
                break;
            }
        }
        if (variable == null) {
            for (HashMap<String, Variable> scope : scopeStack) {
                for (String varname : scope.keySet()) {
                    if (name.equals(varname)) {
                        variable = scope.get(varname);
                        break;
                    }
                }
                if (variable != null) break;
            }
        }
        return variable;
    }
    private void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }
        file.delete();
    }
    public Variable runFunction(String name, int callTokenIndex, Variable... parameters) {
        //Debugger.start("Function " + name);
        Function function = functions.get(name);
        if (function == null) LangError.DEFINE.report(this, "Function '" + name + "' not defined");
        if (parameters.length != function.params.length) LangError.DEFINE.report(this, "Function '" + name + "' takes " + function.params.length + " arguments, found " + parameters.length);
        HashMap<String, Variable> vars = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            if (!function.params[i].types.isEmpty()) {
                boolean isCompatible = false;
                for (VariableType<?> type : function.params[i].types) {
                    if (parameters[i].type == type) {
                        isCompatible = true;
                        break;
                    }
                }
                if (!isCompatible) LangError.TYPE.report(this, nth(i + 1) + " parameter has incompatible type; only accepts: " + commas(function.params[i].types) + "; inputted: " + parameters[i].type.name(), tokens[callTokenIndex].lineNumber, tokens[callTokenIndex].columnNumber);
            }
            vars.put(function.params[i].name, parameters[i]);
        }
        FunctionCall call = new FunctionCall();
        call.scope.push(vars);
        call.callTokenIndex = callTokenIndex;
        callStack.push(call);
        Variable value = function.run(this);
        callStack.pop();
        //Debugger.end();
        return value;
    }
    private String commas(ArrayList<VariableType<?>> types) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            builder.append(types.get(i).name());
            if (i + 1 < types.size()) builder.append(", ");
        }
        return builder.toString();
    }
    public void importCode(String code) {
        Token[] tokens = Tokenizer.tokenize(code);
        RIOARuntime context = new RIOARuntime(tokens);
        context.analyzeTokens();
        merge(context);
    }
    public void merge(RIOARuntime context) {
        globalVariables.putAll(context.globalVariables);
        functions.putAll(context.functions);
    }
    public static class Value<V> {
        public V value;
        public Value() {}
        public Value(V value) {
            this.value = value;
        }
    }
    private String nth(int x) {
        String str = "" + x;
        char last = str.charAt(str.length() - 1);
        String exponent = "th";
        if (last == '1') exponent = "st";
        if (last == '2') exponent = "nd";
        if (last == '3') exponent = "rd";
        return str + exponent;
    }
}
