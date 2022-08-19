package com.rioa.runtime.error;

public interface ErrorCallback {
    void callback(String name, String msg, int lineNumber, int columnNumber);
}
