package com.rioa.token;

import com.rioa.runtime.error.LangError;
import com.rioa.runtime.RIOARuntime;
import com.rioa.runtime.variable.VariableType;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

public class Tokenizer {
    public static Token[] tokenize(String code) {
        return tokenize(code, true);
    }
    public static Token[] tokenize(String code, boolean ignoreComments) {
        ArrayList<Token> tokens = new ArrayList<>();
        int lineNumber = 1;
        int columnNumber = 0;
        String literal = null;
        String word = null;
        int literalColumn = 0;
        int literalLine = 0;
        int wordColumn = 0;
        int wordLine = 0;
        int backslashes = 0;
        for (int i = 0; i < code.length(); i++) {
            char character = code.charAt(i);
            if (!isValidSymbol(character)) LangError.SYNTAX.report("Invalid symbol: " + character + "[\\u" + (int)character + "]", lineNumber, columnNumber);
            if (Character.isWhitespace(character)) {
                if (character == '\n') {
                    if (literal != null) LangError.SYNTAX.report("Unterminated string", literalLine, literalColumn);
                    columnNumber = -1;
                    lineNumber++;
                    if (!tokens.isEmpty()) tokens.get(tokens.size() - 1).endOfLine = true;
                }
                if (word != null) {
                    addWord(word, tokens, wordLine, wordColumn);
                    word = null;
                }
                if (literal != null) {
                    backslashes = 0;
                    literal += character;
                }
            }
            else if (isSymbol(character)) {
                if (word != null) {
                    try {
                        int type = word.startsWith("0x") ? 1 : word.startsWith("0b") ? 2 : word.startsWith("0o") ? 3 : 0;
                        long x = Long.parseLong(type != 0 ? word.substring(2) : word, type == 1 ? 16 : type == 2 ? 2 : type == 3 ? 8 : 10);
                        Token token = new Token();
                        token.columnNumber = wordColumn;
                        token.lineNumber = wordLine;
                        token.type = TokenType.NUMBER;
                        token.value = x;
                        tokens.add(token);
                    }
                    catch (Exception e) {
                        Token token = new Token();
                        token.columnNumber = wordColumn;
                        token.lineNumber = wordLine;
                        token.type = TokenType.WORD;
                        token.value = word;
                        tokens.add(token);
                    }
                    word = null;
                }
                if (character == '\"') {
                    if (literal != null && backslashes % 2 == 0) {
                        Token token = new Token();
                        token.columnNumber = literalColumn;
                        token.lineNumber = literalLine;
                        token.type = TokenType.STRING_LITERAL;
                        token.value = escape(literal);
                        tokens.add(token);
                        literal = null;
                    }
                    else {
                        literal = "";
                        literalColumn = columnNumber;
                        literalLine = lineNumber;
                    }
                }
                else if (character == '\\' && literal != null) {
                    backslashes++;
                    literal += "\\";
                }
                else {
                    if (literal != null) {
                        backslashes = 0;
                        literal += character;
                    }
                    else {
                        Token token = new Token();
                        token.value = character;
                        token.type = TokenType.SYMBOL;
                        token.lineNumber = lineNumber;
                        token.columnNumber = columnNumber;
                        tokens.add(token);
                        if (i > 0) {
                            if (code.charAt(i - 1) == '/') {
                                if (character == '/') {
                                    tokens.remove(tokens.size() - 1);
                                    tokens.remove(tokens.size() - 1);
                                    boolean backslash = false;
                                    int j;
                                    for (j = i + 1; j < code.length(); j++) {
                                        if (code.charAt(j) == '/') backslash = true;
                                        else if (backslash && code.charAt(j) == '/') break;
                                        else backslash = false;
                                    }
                                    i = j;
                                    if (tokens.size() != 0) tokens.get(tokens.size() - 1).endOfLine = true;
                                }
                                if (character == '*') {
                                    tokens.remove(tokens.size() - 1);
                                    tokens.remove(tokens.size() - 1);
                                    boolean multiline = false;
                                    boolean asterisk = false;
                                    int j = 0;
                                    for (j = i + 1; j < code.length(); j++) {
                                        if (code.charAt(j) == '*') asterisk = true;
                                        else if (asterisk && code.charAt(j) == '/') break;
                                        else asterisk = false;
                                        if (code.charAt(j) == '\n') multiline = true;
                                    }
                                    i = j;
                                    if (multiline && tokens.size() != 0) tokens.get(tokens.size() - 1).endOfLine = true;
                                }
                            }
                        }
                    }
                }
            }
            else {
                if (literal != null) {
                    backslashes = 0;
                    literal += character;
                }
                else if (word != null) word += character;
                else {
                    wordLine = lineNumber;
                    wordColumn = columnNumber;
                    word = "" + character;
                }
            }
            columnNumber++;
        }
        if (word != null) addWord(word, tokens, wordLine, wordColumn);
        if (!tokens.isEmpty()) tokens.get(tokens.size() - 1).endOfLine = true;
        Token[] array = new Token[tokens.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = tokens.get(i);
        }
        return array;
    }
    private static boolean isValidSymbol(char character) {
        return character <= 126;
    }
    private static boolean isSymbol(char character) {
        return !(character >= '0' && character <= '9') && !(character >= 'A' && character <= 'Z') && !(character >= 'a' && character <= 'z');
    }
    private static void addWord(String word, ArrayList<Token> tokens, int line, int column) {
        try {
            int type = word.startsWith("0x") ? 1 : word.startsWith("0b") ? 2 : word.startsWith("0o") ? 3 : 0;
            long x = Long.parseLong(type != 0 ? word.substring(2) : word, type == 1 ? 16 : type == 2 ? 2 : type == 3 ? 8 : 10);
            Token token = new Token();
            token.columnNumber = column;
            token.lineNumber = line;
            token.type = TokenType.NUMBER;
            token.value = x;
            tokens.add(token);
        }
        catch (Exception e) {
            Token token = new Token();
            token.columnNumber = column;
            token.lineNumber = line;
            token.type = TokenType.WORD;
            token.value = word;
            tokens.add(token);
        }
    }
    private static String escape(String unescaped) {
        try {
            Properties properties = new Properties();
            properties.load(new StringReader("x=a" + unescaped));
            return properties.getProperty("x").substring(1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String unescape(String escaped) {
        StringBuilder sb = new StringBuilder(escaped.length());
        for (int i = 0; i < escaped.length(); i++) {
            char ch = escaped.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == escaped.length() - 1) ? '\\' : escaped.charAt(i + 1);
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    case 'u':
                        if (i >= escaped.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt("" + escaped.charAt(i + 2) + escaped.charAt(i + 3) + escaped.charAt(i + 4) + escaped.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
    public static String ansiSyntaxHighlight(String code) {
        StringBuilder ansi = new StringBuilder();
        Token[] tokens = tokenize(code, false);
        int l = 1;
        int c = 0;
        for (Token token : tokens) {
            int tl = token.lineNumber;
            int tc = token.columnNumber;
            for (; l < tl; l++) {
                c = 0;
                ansi.append("\n");
            }
            for (; c < tc; c++) {
                ansi.append(" ");
            }
            if (token.type == TokenType.SYMBOL) {
                c++;
                ansi.append(token.value);
            }
            if (token.type == TokenType.NUMBER) {
                String stringified = token.value.toString();
                c += stringified.length();
                ansi.append(getAnsi(ANSI_COLOR_MAGENTA));
                ansi.append(stringified);
                ansi.append(resetAnsi());
            }
            if (token.type == TokenType.WORD) {
                String value = (String)token.value;
                c += value.length();
                if (isKeyword(value)) ansi.append(getAnsi(ANSI_COLOR_YELLOW));
                if (isVariableType(value)) ansi.append(getAnsi(ANSI_COLOR_BLUE));
                if (isBuiltInFunctionName(value)) ansi.append(getAnsi(ANSI_COLOR_RED));
                ansi.append(value);
                ansi.append(resetAnsi());
            }
            if (token.type == TokenType.STRING_LITERAL) {
                String value = (String)token.value;
                c += value.length() + 2;
                ansi.append(getAnsi(ANSI_COLOR_CYAN));
                ansi.append("\"").append(unescape(value)).append("\"");
                ansi.append(resetAnsi());
            }
        }
        return ansi.toString();
    }
    private static final int ANSI_COLOR_RED = 1;
    private static final int ANSI_COLOR_YELLOW = 3;
    private static final int ANSI_COLOR_BLUE = 4;
    private static final int ANSI_COLOR_MAGENTA = 5;
    private static final int ANSI_COLOR_CYAN = 6;
    private static String getAnsi(int color, int... style) {
        return "\u001B[3" + color + "m";
    }
    private static String resetAnsi() {
        return "\u001B[0m";
    }
    private static boolean isKeyword(String keyword) {
        return keyword.equals(RIOARuntime.KW_WHILE) ||
               keyword.equals(RIOARuntime.KW_RETURN) ||
               keyword.equals(RIOARuntime.KW_BREAK) ||
               keyword.equals(RIOARuntime.KW_CONTINUE) ||
               keyword.equals(RIOARuntime.KW_FUNC) ||
               keyword.equals(RIOARuntime.KW_AND) ||
               keyword.equals(RIOARuntime.KW_CATCH) ||
               keyword.equals(RIOARuntime.KW_ELSE) ||
               keyword.equals(RIOARuntime.KW_IF) ||
               keyword.equals(RIOARuntime.KW_IMPORT) ||
               keyword.equals(RIOARuntime.KW_OR) ||
               keyword.equals(RIOARuntime.KW_RUN) ||
               keyword.equals(RIOARuntime.KW_TRY) ||
               keyword.equals(RIOARuntime.KW_TRUE) ||
               keyword.equals(RIOARuntime.KW_FALSE);
    }
    private static boolean isVariableType(String keyword) {
        return keyword.equals(VariableType.ARRAY.name()) ||
               keyword.equals(VariableType.STRING.name()) ||
               keyword.equals(VariableType.NUMBER.name()) ||
               keyword.equals(VariableType.BOOLEAN.name());
    }
    private static boolean isBuiltInFunctionName(String keyword) {
        return new RIOARuntime(null).functions.containsKey(keyword);
    }
}
