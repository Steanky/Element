package com.github.steanky.element.core.key;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Basic implementation of {@link PathSplitter}. A forward slash '/' is interpreted as a delimiter character. '\' is
 * interpreted as an escape character. Index nodes are denoted with a preceding 'i'.
 * <p>
 * This class is a singleton; its instance can be obtained by calling {@link BasicPathSplitter#INSTANCE}.
 */
public class BasicPathSplitter implements PathSplitter {
    /**
     * Singleton instance for this class.
     */
    public static final PathSplitter INSTANCE = new BasicPathSplitter();

    private static final String EMPTY = "";
    private static final char DELIMITER = '/';
    private static final char ESCAPE = '\\';
    private static final char INTEGER_INDICATOR = 'i';
    private static final int EXPECTED_NODE_WIDTH = 6;

    private BasicPathSplitter() {}

    @Override
    public @NotNull Object @NotNull [] splitPathKey(final @NotNull String pathString) {
        if (pathString.isEmpty()) {
            return new Object[] {EMPTY};
        }

        //come up with a reasonable guess for the number of elements in the path string
        final int length = pathString.length();
        final List<Object> objects = new ArrayList<>(Math.max(4, length / EXPECTED_NODE_WIDTH));

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
            }
            else if (current == ESCAPE) {
                escape = true;
            }
            else if (current == DELIMITER) {
                if (i != 0) {
                    handleNode(objects, nodeBuffer.toString());
                    nodeBuffer.setLength(0);
                }
            }
            else {
                nodeBuffer.append(current);
            }
        }

        if (!nodeBuffer.isEmpty()) {
            handleNode(objects, nodeBuffer.toString());
        }

        return objects.toArray();
    }

    private static void handleNode(final Collection<? super Object> objects, final String substring) {
        if (substring.isEmpty()) {
            objects.add(substring);
            return;
        }

        final char first = substring.charAt(0);
        if (first == INTEGER_INDICATOR) {
            final String numberPart = substring.substring(1);
            try {
                objects.add(Integer.parseInt(numberPart));
                return;
            }
            catch (NumberFormatException ignored) {}

            objects.add(numberPart);
        }
        else if (substring.length() > 1 && first == ESCAPE && substring.charAt(1) == INTEGER_INDICATOR) {
            objects.add(substring.substring(1));
        }
        else {
            objects.add(substring);
        }
    }

    @Override
    public @NotNull String normalize(final @NotNull String pathString) {
        if (pathString.isEmpty()) {
            return EMPTY;
        }

        final int length = pathString.length();
        int startIndex = 0;
        if (pathString.charAt(0) == DELIMITER) {
            if (length == 1) {
                return EMPTY;
            }

            if (pathString.charAt(1) != DELIMITER) {
                startIndex++;
            }
        }

        int endIndex = length - 1;
        if (pathString.charAt(endIndex) == DELIMITER) {
            if (length == 2) {
                return EMPTY;
            }

            if (pathString.charAt(endIndex - 1) != DELIMITER) {
                endIndex--;
            }
        }

        return pathString.substring(startIndex, endIndex + 1);
    }

    @Override
    public @NotNull String append(final @NotNull String pathString, final @NotNull Object element) {
        return normalize(normalize(pathString) + DELIMITER + element);
    }
}