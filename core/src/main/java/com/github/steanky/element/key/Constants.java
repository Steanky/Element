package com.github.steanky.element.key;

import org.intellij.lang.annotations.Language;

public final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    public static final char NAMESPACE_SEPARATOR = ':';

    public static final String NAMESPACED_KEY = "a:a";
    public static final String NAMESPACE_OR_KEY = "a";

    @Language("RegExp")
    public static final String NAMESPACE_PATTERN = "[a-z\\d_\\-.]+";

    @Language("RegExp")
    public static final String VALUE_PATTERN = "[a-z\\d_\\-./]+";

    @Language("RegExp")
    public static final String KEY_PATTERN = "(" + Constants.NAMESPACE_PATTERN + ":)?" + Constants.VALUE_PATTERN;
}
