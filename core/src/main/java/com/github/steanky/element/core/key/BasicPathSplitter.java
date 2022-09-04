package com.github.steanky.element.core.key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Basic implementation of {@link PathSplitter}. A forward slash '/' is interpreted as a delimiter character. Empty
 * path nodes are ignored.
 */
public class BasicPathSplitter implements PathSplitter {
    private static final String SPLIT_STRING = "/";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    @Override
    public Object @NotNull [] splitPathKey(final @NotNull String pathString) {
        if (pathString.isEmpty()) {
            return new Object[0];
        }

        final String[] entries = pathString.split(SPLIT_STRING);
        final List<Object> objects = new ArrayList<>(entries.length);

        for (final String entry : entries) {
            if (entry.isEmpty()) {
                continue;
            }

            if (NUMBER_PATTERN.matcher(entry).matches()) {
                try {
                    objects.add(Integer.parseInt(entry));
                    continue;
                } catch (NumberFormatException ignored) {}
            }

            objects.add(entry);
        }

        return objects.toArray();
    }

    @Override
    public @NotNull String normalize(final @NotNull String pathString) {
        final Object[] path = splitPathKey(pathString);

        final StringBuilder builder = new StringBuilder(pathString.length());
        for (int i = 0; i < path.length; i++) {
            builder.append(path[i]);

            if (i < path.length - 1) {
                builder.append(SPLIT_STRING);
            }
        }

        return builder.toString();
    }

    @Override
    public @NotNull String append(final @NotNull String pathString, final @NotNull Object element) {
        return normalize(pathString) + SPLIT_STRING + element;
    }
}
