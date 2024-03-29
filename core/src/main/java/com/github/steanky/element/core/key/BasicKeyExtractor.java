package com.github.steanky.element.core.key;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.steanky.element.core.util.Validate.elementException;

/**
 * Basic implementation of {@link KeyExtractor} which uses a {@link KeyParser} to parse named keys.
 */
public class BasicKeyExtractor implements KeyExtractor {
    private final String keyName;
    private final KeyParser keyParser;

    /**
     * Create a new instance of this class.
     *
     * @param keyName   the name of the key
     * @param keyParser the {@link KeyParser} instance used to parse strings into {@link Key} instances
     */
    public BasicKeyExtractor(final @NotNull String keyName, final @NotNull KeyParser keyParser) {
        this.keyName = Objects.requireNonNull(keyName);
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull Key extractKey(final @NotNull ConfigNode node) {
        try {
            @Subst(Constants.NAMESPACE_OR_KEY) final String keyString = node.atOrThrow(keyName).asStringOrThrow();
            return keyParser.parseKey(keyString);
        } catch (ConfigProcessException e) {
            throw elementException("Missing type key " + keyName);
        }
    }

    @Override
    public boolean hasKey(final @NotNull ConfigNode node) {
        if (node.containsKey(keyName)) {
            final ConfigElement element = node.at(keyName);

            if (element != null && element.isString()) {
                return keyParser.isValidKey(element.asString());
            }
        }

        return false;
    }

    @Override
    public void removeKey(final @NotNull ConfigNode node) {
        node.remove(keyName);
    }
}
