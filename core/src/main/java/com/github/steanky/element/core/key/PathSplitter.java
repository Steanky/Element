package com.github.steanky.element.core.key;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Splits "path strings" into string arrays, with support for escaping delimiter characters, generally with a '\',
 * though this is implementation-defined.
 */
public interface PathSplitter {
    /**
     * Splits the given path string.
     *
     * @param pathString the path to split
     * @return the path's components
     */
    @NotNull String @NotNull [] splitPathKey(final @NotNull String pathString);

    /**
     * "Normalizes" the given path string. This is done so that different path string representations that point to the
     * same element can be converted to a single, "true" representation, for use as keys or storage.
     *
     * @param pathKey the path string to normalize
     * @return the normalized string
     */
    @NotNull String normalize(final @NotNull String pathKey);

    /**
     * Modifies the given string, such that any significant characters (like delimiters) are escaped. The returned value
     * will be normalized.
     *
     * @param pathNode the string to modify
     * @return the escaped string
     */
    @NotNull String escape(final @NotNull String pathNode);

    /**
     * Appends a new element onto the given path string. This will add at most 1 to the length of the array that would
     * be returned by {@link PathSplitter#splitPathKey(String)}, because significant characters in {@code element}'s
     * string representation will be escaped, if necessary.
     *
     * @param pathString the current path string
     * @param element    the element to append to the path string
     * @return the new path string
     */
    @NotNull String append(final @NotNull String pathString, final @NotNull Object element);

    /**
     * Follows the given path, starting from the given root element. Supports indexing into lists (if the corresponding
     * path node may be parsed to an int). If the path is not accessible, throws an {@link ElementException}.
     *
     * @param root the root node
     * @param path the path array
     * @return the element located at the path
     */
    @NotNull ConfigElement findElement(final @NotNull ConfigElement root, final @NotNull String @NotNull [] path);

    /**
     * Convenience overload for {@link PathSplitter#findElement(ConfigElement, String[])} that additionally requires
     * that the target element is a {@link ConfigContainer}. If the path is not accessible, or the element is not of the
     * right type, throws an {@link ElementException}.
     *
     * @param root the root node
     * @param path the path array
     * @return the element located at the path
     */
    default @NotNull ConfigContainer findContainer(final @NotNull ConfigElement root,
            final @NotNull String @NotNull [] path) {
        final ConfigElement element = findElement(root, path);
        if (!element.isContainer()) {
            throw new ElementException(
                    "expected a container, got " + element.type() + " at path " + Arrays.toString(path));
        }

        return element.asContainer();
    }

    /**
     * Convenience overload for {@link PathSplitter#findElement(ConfigElement, String[])} that additionally requires
     * that the target element is a {@link ConfigNode}. If the path is not accessible, or the element is not of the
     * right type, throws an {@link ElementException}.
     *
     * @param root the root node
     * @param path the path array
     * @return the element located at the path
     */
    default @NotNull ConfigNode findNode(final @NotNull ConfigElement root, final @NotNull String @NotNull [] path) {
        final ConfigElement element = findElement(root, path);
        if (!element.isNode()) {
            throw new ElementException("expected a node, got " + element.type() + " at path " + Arrays.toString(path));
        }

        return element.asNode();
    }

    /**
     * Convenience overload for {@link PathSplitter#findElement(ConfigElement, String[])} that additionally requires
     * that the target element is a {@link ConfigList}. If the path is not accessible, or the element is not of the
     * right type, throws an {@link ElementException}.
     *
     * @param root the root node
     * @param path the path array
     * @return the element located at the path
     */
    default @NotNull ConfigList findList(final @NotNull ConfigElement root, final @NotNull String @NotNull [] path) {
        final ConfigElement element = findElement(root, path);
        if (!element.isList()) {
            throw new ElementException("expected a list, got " + element.type() + " at path " + Arrays.toString(path));
        }

        return element.asList();
    }
}
