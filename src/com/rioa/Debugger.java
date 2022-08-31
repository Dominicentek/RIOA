package com.rioa;

import java.util.Stack;

public class Debugger {
    private static final Stack<Long> time = new Stack<>();
    private static final Stack<String> tasks = new Stack<>();
    public static void start(String task) {
        if (!Main.debug) return;
        time.push(System.nanoTime());
        tasks.push(task);
        System.out.println(addSpaces() + "[DEBUG] " + task + "...");
    }
    public static void end() {
        if (!Main.debug) return;
        double time = Math.round((System.nanoTime() - Debugger.time.pop()) / 1000.0) / 1000.0;
        System.out.println(addSpaces() + "[DEBUG] " + tasks.pop() + " took " + time + "ms");
    }
    public static void endAll() {
        while (!tasks.empty()) {
            end();
        }
    }
    private static String addSpaces() {
        String spaces = "";
        for (int i = 1; i < tasks.size(); i++) {
            spaces += "  ";
        }
        return spaces;
    }
}
