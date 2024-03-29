package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link KeyParser}. Supports variable "default" namespaces for {@link Key} objects to have,
 * which will be used as the namespace when it is unspecified by the input string.
 *
 * @implNote Although similar, {@link BasicKeyParser#parseKey(String)} and {@link Key#key(String)} have subtly different
 * behaviors when handling empty namespaces. BasicKeyParser will treat key strings like {@code :test} as having a
 * namespace which is an empty string, with the value {@code test}. Key will use the default namespace,
 * {@link Key#MINECRAFT_NAMESPACE}, in this case. BasicKeyParser will only use its default namespace <i>if there is no
 * separator character present</i>.
 */
public class BasicKeyParser implements KeyParser {
    /**
     * The default-default namespace for this {@link KeyParser} implementation.
     */
    public static final String DEFAULT_DEFAULT_NAMESPACE = "default";

    private final String defaultNamespace;

    /**
     * Creates a new instance of this class.
     *
     * @param defaultNamespace the default namespace, which cannot be null or nonconforming to its pattern
     * @throws IllegalArgumentException if defaultNamespace is null, empty, or otherwise does not conform to its
     *                                  pattern
     */
    public BasicKeyParser(final @NotNull @NamespaceString String defaultNamespace) {
        if (!namespaceValid(defaultNamespace)) {
            throw new IllegalArgumentException("invalid default namespace '" + defaultNamespace + "'");
        }

        this.defaultNamespace = defaultNamespace;
    }

    /**
     * Creates a new instance of this class that uses the default namespace string
     * {@link BasicKeyParser#DEFAULT_DEFAULT_NAMESPACE}.
     */
    public BasicKeyParser() {
        this(DEFAULT_DEFAULT_NAMESPACE);
    }

    //below logic currently duplicates what is in KeyImpl, this should no longer be necessary after Adventure 4.12.0
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean namespaceValid(final String namespace) {
        for (int i = 0, length = namespace.length(); i < length; i++) {
            if (!validNamespaceChar(namespace.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean valueValid(final String value) {
        for (int i = 0, length = value.length(); i < length; i++) {
            if (!validValueChar(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean validNamespaceChar(final int value) {
        return value == '_' || value == '-' || (value >= 'a' && value <= 'z') || (value >= '0' && value <= '9') ||
                value == '.';
    }

    private static boolean validValueChar(final int value) {
        return validNamespaceChar(value) || value == '/';
    }

    private String extractNamespace(String keyString, int separatorIndex) {
        return separatorIndex >= 0 ? keyString.substring(0, separatorIndex) : defaultNamespace;
    }

    private String extractValue(String keyString, int separatorIndex) {
        return separatorIndex >= 0 ? keyString.substring(separatorIndex + 1) : keyString;
    }

    @Override
    public @NotNull Key parseKey(final @NotNull @KeyString String keyString) {
        final int separatorIndex = keyString.indexOf(Constants.NAMESPACE_SEPARATOR);

        //resolve default namespaces differently than in adventure: leading : means empty namespace, no : means default
        @Subst(Constants.NAMESPACE_OR_KEY) final String namespace = extractNamespace(keyString, separatorIndex);
        if (!namespaceValid(namespace)) {
            throw elementException("Invalid namespace " + namespace + " from key " + keyString);
        }

        @Subst(Constants.NAMESPACE_OR_KEY) final String value = extractValue(keyString, separatorIndex);
        if (!valueValid(value)) {
            throw elementException("Invalid value " + value + " from key " + keyString);
        }

        return Key.key(namespace, value);
    }

    @Override
    public boolean isValidKey(@NotNull String keyString) {
        final int separatorIndex = keyString.indexOf(Constants.NAMESPACE_SEPARATOR);
        return namespaceValid(extractNamespace(keyString, separatorIndex)) &&
                valueValid(extractValue(keyString, separatorIndex));
    }

    @Override
    public @NotNull String defaultNamespace() {
        return defaultNamespace;
    }
}