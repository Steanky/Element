package com.github.steanky.element.core.key;

import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation of {@link PathKeySplitter}. A forward slash '/' is interpreted as a delimiter character.
 */
public class BasicPathKeySplitter implements PathKeySplitter {
    private static final String SPLIT_STRING = "/";

    @Override
    public String @NotNull [] splitPathKey(final @NotNull Key pathKey) {
        return pathKey.value().split(SPLIT_STRING);
    }

    @Override
    public @NotNull Key normalize(final @NotNull Key pathKey) {
        final String[] path = splitPathKey(pathKey);
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            builder.append(path[i]);

            if (i < path.length - 1) {
                builder.append(SPLIT_STRING);
            }
        }

        @Subst(Constants.NAMESPACE_OR_KEY) final String namespace = pathKey.namespace();
        @Subst(Constants.NAMESPACE_OR_KEY) final String value = builder.toString();
        return Key.key(namespace, value);
    }
}
