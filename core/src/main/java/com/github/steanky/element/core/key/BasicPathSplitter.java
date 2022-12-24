package com.github.steanky.element.core.key;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Basic implementation of {@link PathSplitter}. A forward slash '/' is interpreted as a delimiter character. '\' is
 * interpreted as an escape character.
 * <p>
 * This class is a singleton; its instance can be obtained by calling {@link BasicPathSplitter#INSTANCE}.
 */
public class BasicPathSplitter implements PathSplitter {
    /**
     * Singleton instance for this class.
     */
    public static final PathSplitter INSTANCE = new BasicPathSplitter();

    private static final char DELIMITER = '/';
    private static final char ESCAPE = '\\';
    private static final int EXPECTED_NODE_WIDTH = 6;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private BasicPathSplitter() {}

    private static void handleNode(final Collection<? super String> nodes, final String substring) {
        nodes.add(substring);
    }

    @Override
    public @NotNull String @NotNull [] splitPathKey(final @NotNull String pathString) {
        if (pathString.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }

        //come up with a reasonable guess for the number of elements in the path string
        final int length = pathString.length();
        final List<String> nodes = new ArrayList<>(Math.max(4, length / EXPECTED_NODE_WIDTH));

        final StringBuilder nodeBuffer = new StringBuilder(EXPECTED_NODE_WIDTH);
        boolean escape = false;
        for (int i = 0; i < pathString.length(); i++) {
            final char current = pathString.charAt(i);

            if (escape) {
                if (!(current == ESCAPE || current == DELIMITER)) {
                    //re-add the escape char if it didn't actually escape anything significant
                    nodeBuffer.append(ESCAPE);
                }

                nodeBuffer.append(current);
                escape = false;
            } else if (current == ESCAPE) {
                escape = true;
            } else if (current == DELIMITER) {
                if (i != 0) {
                    handleNode(nodes, nodeBuffer.toString());
                    nodeBuffer.setLength(0);
                }
            } else {
                nodeBuffer.append(current);
            }
        }

        if (!nodeBuffer.isEmpty()) {
            handleNode(nodes, nodeBuffer.toString());
        }

        return nodes.toArray(EMPTY_STRING_ARRAY);
    }

    @Override
    public @NotNull String normalize(final @NotNull String pathString) {
        if (pathString.isEmpty()) {
            return StringUtils.EMPTY;
        }

        final int length = pathString.length();
        int startIndex = 0;
        if (pathString.charAt(0) == DELIMITER) {
            if (length == 1) {
                return StringUtils.EMPTY;
            }

            if (pathString.charAt(1) != DELIMITER) {
                startIndex++;
            }
        }

        int endIndex = length - 1;
        if (pathString.charAt(endIndex) == DELIMITER) {
            if (length == 2) {
                return StringUtils.EMPTY;
            }

            if (pathString.charAt(endIndex - 1) != DELIMITER) {
                endIndex--;
            }
        }

        return pathString.substring(startIndex, endIndex + 1);
    }

    @Override
    public @NotNull String escape(@NotNull String pathNode) {
        pathNode = normalize(pathNode);

        final StringBuilder builder = new StringBuilder(pathNode.length() + 5);

        boolean escape = false;
        for (int i = 0; i < pathNode.length(); i++) {
            final char character = pathNode.charAt(i);
            if (escape) {
                if (character != DELIMITER) {
                    builder.append(ESCAPE);
                }

                escape = false;
            } else if (character == DELIMITER) {
                builder.append(ESCAPE);
            } else if (character == ESCAPE) {
                escape = true;
            }

            builder.append(character);
        }

        return builder.toString();
    }

    @Override
    public @NotNull String append(final @NotNull String pathString, final @NotNull Object element) {
        return normalize(pathString) + DELIMITER + escape(normalize(element.toString()));
    }

    @Override
    public @NotNull ConfigElement findElement(@NotNull ConfigElement root, @NotNull String @NotNull [] path) {
        ConfigElement current = Objects.requireNonNull(root);

        for (final String node : path) {
            Objects.requireNonNull(node);

            if (current.isNode()) {
                current = current.asNode().get(node);
            } else if (current.isList()) {
                try {
                    final int index = Integer.parseInt(node);
                    final ConfigList list = current.asList();

                    if (index > -1 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        current = null;
                    }
                } catch (NumberFormatException e) {
                    throw new ElementException(
                            "invalid path array " + Arrays.toString(path) + ", expected parseable index, got " + node, e);
                }
            }

            if (current == null) {
                throw new ElementException("invalid path array " + Arrays.toString(path) + ", no such node " + node);
            }
        }

        return current;
    }
}