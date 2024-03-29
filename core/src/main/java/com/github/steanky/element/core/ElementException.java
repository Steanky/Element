package com.github.steanky.element.core;

import com.github.steanky.ethylene.core.path.ConfigPath;

/**
 * Represents a generic exception thrown by various parts of the Element API. Can have an associated {@link ConfigPath}
 * and {@link Class} which, respectively, define a path and/or class related to this error.
 */
public class ElementException extends RuntimeException {
    /**
     * The element class associated with this error.
     */
    private Class<?> elementClass;

    /**
     * The element path associated with this error.
     */
    private ConfigPath configPath;

    /**
     * Creates a new ElementException with no detail message or cause.
     */
    public ElementException() {
        super();
    }

    /**
     * Creates a new ElementException with the given detail message and no cause.
     *
     * @param message the detail message
     */
    public ElementException(String message) {
        super(message);
    }

    /**
     * Creates a new ElementException with the given detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of this exception
     */
    public ElementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new ElementException with the given cause and no detail message.
     *
     * @param cause the cause of this exception
     */
    public ElementException(Throwable cause) {
        super(cause);
    }

    /**
     * Sets the element class associated with this error. This method does nothing if an element class is already set.
     *
     * @param elementClass the element class
     */
    public void setElementClass(Class<?> elementClass) {
        if (this.elementClass == null) {
            this.elementClass = elementClass;
        }
    }

    /**
     * Sets the {@link ConfigPath} associated with this error. This method does nothing if an error path is already set.
     *
     * @param configPath the path
     */
    public void setConfigPath(ConfigPath configPath) {
        if (this.configPath == null) {
            this.configPath = configPath;
        }
    }

    /**
     * The element class which had an error.
     *
     * @return the element class
     */
    public Class<?> elementClass() {
        return elementClass;
    }

    /**
     * The path of the element object which had an error.
     *
     * @return the error path
     */
    public ConfigPath errorPath() {
        return configPath;
    }

    @Override
    public String getMessage() {
        final String baseMessage = super.getMessage();
        final StringBuilder builder = new StringBuilder(baseMessage.length());
        final Class<?> elementClass = this.elementClass;
        final ConfigPath elementPath = this.configPath;

        builder.append('\"').append(baseMessage).append('\"');
        if (elementClass != null) {
            builder.append(System.lineSeparator()).append("Relevant class: ").append(elementClass);
        }

        if (elementPath != null) {
            builder.append(System.lineSeparator()).append("Data path: '").append(elementPath).append('\'');
        }


        return builder.toString();
    }
}
