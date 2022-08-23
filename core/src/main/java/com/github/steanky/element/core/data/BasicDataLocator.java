package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.PathKeySplitter;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Basic implementation of {@link DataLocator}.
 */
public class BasicDataLocator implements DataLocator {
    private final PathKeySplitter pathKeySplitter;

    /**
     * Creates a new instance of this class.
     *
     * @param pathKeySplitter the {@link PathKeySplitter} used to split path keys
     */
    public BasicDataLocator(final @NotNull PathKeySplitter pathKeySplitter) {
        this.pathKeySplitter = Objects.requireNonNull(pathKeySplitter);
    }

    @Override
    public @NotNull ConfigNode locate(final @NotNull ConfigNode rootNode, final @Nullable String dataPath) {
        if (dataPath == null) {
            return rootNode;
        }

        final Object[] path = pathKeySplitter.splitPathKey(dataPath);
        try {
            return rootNode.getNodeOrThrow(path);
        }
        catch (ConfigProcessException e) {
            throw new ElementException("invalid data path '" + dataPath + "'", e);
        }
    }
}
