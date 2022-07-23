package com.github.steanky.element;

/**
 * Represents a generic exception thrown by various parts of the Element API.
 */
public class ElementException extends RuntimeException {
    /**
     * Creates a new ElementException with no detail message or cause.
     */
    public ElementException() {
        super();
    }

    /**
     * Creates a new ElementException with the given detail message and no cause.
     * @param message the detail message
     */
    public ElementException(String message) {
        super(message);
    }

    /**
     * Creates a new ElementException with the given detail message and cause.
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ElementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new ElementException with the given cause and no detail message.
     * @param cause the cause of this exception
     */
    public ElementException(Throwable cause) {
        super(cause);
    }
}
