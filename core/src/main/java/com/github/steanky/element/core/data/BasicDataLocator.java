package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.key.PathSplitter;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Basic implementation of {@link DataLocator}.
 */
public class BasicDataLocator implements DataLocator {
    private final PathSplitter pathSplitter;

    /**
     * Creates a new instance of this class.
     *
     * @param pathSplitter the {@link PathSplitter} used to split path keys
     */
    public BasicDataLocator(final @NotNull PathSplitter pathSplitter) {
        this.pathSplitter = Objects.requireNonNull(pathSplitter);
    }

    @Override
    public @NotNull ConfigNode locate(final @NotNull ConfigNode rootNode, final @Nullable String dataPath) {
        if (dataPath == null) {
            return rootNode;
        }

        final Object[] path = pathSplitter.splitPathKey(dataPath);
        try {
            return rootNode.getNodeOrThrow(path);
        }
        catch (ConfigProcessException e) {
            throw new ElementException("invalid data path '" + dataPath + "'", e);
        }
    }
}
