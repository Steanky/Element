package com.github.steanky.element.core.key;

import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Subst;

/**
 * Holds Key-related constants.
 */
public final class Constants {
    /**
     * The namespace separator character.
     */
    public static final char NAMESPACE_SEPARATOR = ':';
    /**
     * An example of a key that supplies an explicit namespace. Useful for passing to {@link Subst}.
     */
    public static final String NAMESPACED_KEY = "a:a";
    /**
     * An example of both a namespace and a key. Useful for passing to {@link Subst}.
     */
    public static final String NAMESPACE_OR_KEY = "a";
    /**
     * The regex pattern all key namespaces must conform to.
     */
    @Language("RegExp")
    public static final String NAMESPACE_PATTERN = "[a-z\\d_\\-.]+";
    /**
     * The regex pattern all key values must conform to.
     */
    @Language("RegExp")
    public static final String VALUE_PATTERN = "[a-z\\d_\\-./]+";
    /**
     * The regex pattern all key strings must conform to.
     */
    @Language("RegExp")
    public static final String KEY_PATTERN = "(" + Constants.NAMESPACE_PATTERN + ":)?" + Constants.VALUE_PATTERN;

    private Constants() {
        throw new UnsupportedOperationException();
    }
}
