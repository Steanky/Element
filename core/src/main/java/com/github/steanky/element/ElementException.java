package com.github.steanky.element;

public class ElementException extends RuntimeException {
    public ElementException() {
        super();
    }

    public ElementException(String message) {
        super(message);
    }

    public ElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElementException(Throwable cause) {
        super(cause);
    }
}
