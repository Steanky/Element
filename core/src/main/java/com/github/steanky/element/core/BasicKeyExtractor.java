package com.github.steanky.element.core;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic implementation of {@link KeyExtractor} which uses a {@link KeyParser} to parse named keys.
 */
public class BasicKeyExtractor implements KeyExtractor {
    private final String keyName;
    private final KeyParser keyParser;

    /**
     * Create a new instance of this class.
     *
     * @param keyName   the name of the key passed to {@link ConfigElement#getStringOrThrow(Object...)}
     * @param keyParser the {@link KeyParser} instance used to parse strings into {@link Key} instances
     */
    public BasicKeyExtractor(final @NotNull String keyName, final @NotNull KeyParser keyParser) {
        this.keyName = Objects.requireNonNull(keyName);
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    public @NotNull Key extract(final @NotNull ConfigNode node) {
        try {
            @Subst(Constants.NAMESPACE_OR_KEY) final String keyString = node.getStringOrThrow(keyName);
            return keyParser.parseKey(keyString);
        } catch (ConfigProcessException e) {
            throw new ElementException("Failed to extract key", e);
        }
    }
}
