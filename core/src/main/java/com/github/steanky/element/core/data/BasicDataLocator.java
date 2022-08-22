package com.github.steanky.element.core.data;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.key.PathKeySplitter;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Basic implementation of {@link DataLocator}.
 */
public class BasicDataLocator implements DataLocator {
    private final KeyExtractor idExtractor;
    private final PathKeySplitter pathKeySplitter;

    /**
     * Creates a new instance of this class.
     *
     * @param idExtractor     the {@link KeyExtractor} used to extract identifier keys
     * @param pathKeySplitter the {@link PathKeySplitter} used to split path keys
     */
    public BasicDataLocator(final @NotNull KeyExtractor idExtractor, final @NotNull PathKeySplitter pathKeySplitter) {
        this.idExtractor = Objects.requireNonNull(idExtractor);
        this.pathKeySplitter = Objects.requireNonNull(pathKeySplitter);
    }

    @Override
    public @NotNull ConfigNode locate(final @NotNull ConfigNode rootNode, final @Nullable Key dataPath) {
        if (dataPath == null) {
            return rootNode;
        }

        final String dataNamespace = dataPath.namespace();
        final String[] path = pathKeySplitter.splitPathKey(dataPath);

        ConfigNode current = rootNode;
        for (final String name : path) {
            boolean foundNode = false;
            for (final ConfigElement element : current.values()) {
                if (element.isNode()) {
                    final ConfigNode node = element.asNode();
                    final Key id = idExtractor.extractKey(node);

                    if (id.namespace().equals(dataNamespace) && id.value().equals(name)) {
                        current = node;
                        foundNode = true;
                        break;
                    }
                }
            }

            if (!foundNode) {
                throw new ElementException(
                        "couldn't find node named '" + name + "' in node " + current + " under namespace '" +
                                dataNamespace + "' with full path '" + dataPath + "'");
            }
        }

        return current;
    }
}
