package com.github.steanky.element.core.key;

import com.github.steanky.element.core.ElementException;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation of {@link KeyParser}. Supports variable "default" namespaces for {@link Key} objects to have,
 * which will be used as the namespace when it is unspecified by the input string.
 */
public class BasicKeyParser implements KeyParser {
    @Subst(Constants.NAMESPACE_OR_KEY)
    private final String defaultNamespace;

    /**
     * Creates a new instance of this class.
     *
     * @param defaultNamespace the default namespace, which cannot be null or nonconforming to its pattern
     *
     * @throws IllegalArgumentException if defaultNamespace is null, empty, or otherwise does not conform to its
     *                                  pattern
     */
    public BasicKeyParser(final @NotNull @Pattern(Constants.NAMESPACE_PATTERN) String defaultNamespace) {
        if (defaultNamespace.isEmpty()) {
            throw new IllegalArgumentException("Empty namespace not allowed");
        }

        for (final char character : defaultNamespace.toCharArray()) {
            if (!validNamespaceChar(character)) {
                throw new IllegalArgumentException("Invalid default namespace: " + defaultNamespace);
            }
        }

        this.defaultNamespace = defaultNamespace;
    }

    /**
     * Creates a new instance of this class that uses the default namespace {@link Key#MINECRAFT_NAMESPACE}.
     */
    public BasicKeyParser() {
        this(Key.MINECRAFT_NAMESPACE);
    }

    private static boolean validNamespaceChar(final int value) {
        return value == '_' || value == '-' || (value >= 'a' && value <= 'z') || (value >= '0' && value <= '9') ||
                value == '.';
    }

    private static boolean hasExplicitNamespace(final String validKeyString) {
        return validKeyString.indexOf(Constants.NAMESPACE_SEPARATOR) > 0;
    }

    private static Key parseInput(final @Subst(Constants.NAMESPACED_KEY) String input) {
        try {
            return Key.key(input);
        } catch (InvalidKeyException e) {
            throw new ElementException("Illegal key string " + input, e);
        }
    }

    @Override
    public @NotNull Key parseKey(final @NotNull @Subst(Constants.NAMESPACE_OR_KEY) String keyString) {
        @Subst(Constants.NAMESPACE_OR_KEY)
        final Key key = parseInput(keyString);

        if (!hasExplicitNamespace(keyString)) {
            return Key.key(defaultNamespace, keyString);
        }

        return key;
    }
}
