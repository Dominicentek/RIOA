package com.rioa;

import com.rioa.expression.ExpressionException;
import com.rioa.runtime.error.LangError;
import com.rioa.runtime.RIOARuntime;
import com.rioa.runtime.variable.Variable;
import com.rioa.token.Token;
import com.rioa.token.Tokenizer;
import java.io.*;
import java.util.HashMap;

public class Main {
    public static boolean debug = false;
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Debugger::endAll));
        try {
            String sourcefile = null;
            HashMap<String, Boolean> options = new HashMap<>();
            options.put("output", false);
            options.put("highlight", false);
            options.put("run", false);
            options.put("debug", false);
            int i = 0;
            try {
                if (args.length == 0) throw new Exception();
                for (String arg : args) {
                    i++;
                    if (arg.startsWith("-")) {
                        String option = arg.substring(1);
                        if (!options.containsKey(option)) throw new Exception();
                        options.put(option, true);
                    }
                    else {
                        sourcefile = arg;
                        break;
                    }
                }
                if (sourcefile == null) throw new Exception();
            }
            catch (Exception e) {
                System.out.println("Usage:");
                System.out.println("java -jar RIOA.jar [options] sourcefile [args...]");
                System.out.println();
                System.out.println("-output    : Outputs the returning value of main function");
                System.out.println("-highlight : Outputs syntax highlighted ANSI formatted text file with source code");
                System.out.println("-run       : Forcefully runs the code (used with -highlight to run both actions)");
                System.out.println("-debug     : Runs in debugging mode");
                System.exit(0);
            }
            InputStream stream = new FileInputStream(sourcefile);
            byte[] data = new byte[stream.available()];
            stream.read(data);
            stream.close();
            debug = options.get("debug");
            Debugger.start("Executing");
            if (options.get("highlight")) {
                OutputStream out = new FileOutputStream("ansi.txt");
                out.write(Tokenizer.ansiSyntaxHighlight(new String(data)).getBytes());
                out.close();
            }
            if (!options.get("highlight") || options.get("run")) {
                Token[] tokens = Tokenizer.tokenize(new String(data));
                String[] arguments = new String[args.length - i];
                System.arraycopy(args, i, arguments, 0, arguments.length);
                Variable returnValue = new RIOARuntime(tokens).run(arguments);
                if (options.get("output")) System.out.println(returnValue);
            }
            Debugger.end();
        }
        catch (IOException e) {
            LangError.IO.report(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            LangError.INTERNAL.report(e.toString());
        }
    }
}
