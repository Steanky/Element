package com.github.steanky.element.key;

import com.github.steanky.element.ElementException;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public class BasicKeyParser implements KeyParser {
    @Subst(Constants.NAMESPACE_OR_KEY)
    private final String defaultNamespace;

    public BasicKeyParser(final @NotNull @Pattern(Constants.NAMESPACE_PATTERN) String defaultNamespace) {
        for(final char character : defaultNamespace.toCharArray()) {
            if(!validNamespaceChar(character)) {
                throw new IllegalArgumentException("Invalid default namespace: " + defaultNamespace);
            }
        }

        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public @NotNull Key parseKey(final @NotNull @Subst(Constants.NAMESPACE_OR_KEY) String keyString) {
        @Subst(Constants.NAMESPACE_OR_KEY)
        final Key key = parseInput(keyString);

        if(!hasExplicitNamespace(keyString)) {
            return Key.key(defaultNamespace, keyString);
        }

        return key;
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
        }
        catch (InvalidKeyException e) {
            throw new ElementException("Illegal key string " + input, e);
        }
    }
}
