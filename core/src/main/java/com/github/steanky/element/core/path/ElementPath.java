package com.github.steanky.element.core.path;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Represents a particular path through a configuration tree. Implementations of this interface should be
 * equality-comparable with all other implementations of this interface. Therefore, equality (and hash code) should be
 * determined using the list returned by {@link ElementPath#nodes()}. Furthermore, implementations must be immutable;
 * their nodes list should never change throughout their lifetime.
 */
public interface ElementPath {
    /**
     * The empty path, pointing at the root node.
     */
    ElementPath EMPTY = BasicElementPath.EMPTY_PATH;

    /**
     * Parses the given string, assuming UNIX-style formatting and semantics. The resulting path will be normalized such
     * that redundant nodes are removed.
     *
     * @param path the path string to parse
     * @return a new ElementPath
     */
    static @NotNull ElementPath of(final @NotNull String path) {
        Objects.requireNonNull(path);
        return BasicElementPath.parse(path);
    }

    /**
     * Gets the nodes represented by this path.
     *
     * @return the nodes represented by this path
     */
    @NotNull @Unmodifiable List<Node> nodes();

    /**
     * Determines if this path represents an absolute or a relative path.
     *
     * @return true if this path is absolute; false otherwise
     */
    boolean isAbsolute();

    /**
     * Computes a new path by considering the given relative path as relative to this one. If the given path is not
     * actually relative, it will be returned as-is. Otherwise, the relative path's nodes will be appropriately added to
     * this path's, and a new path will be returned.
     * <p>
     * This method is analogous to {@link Path#resolve(Path)}.
     *
     * @param relativePath the relative path
     * @return a new path representing the combination of this path and another
     */
    @NotNull ElementPath resolve(final @NotNull ElementPath relativePath);

    /**
     * Convenience overload for {@link ElementPath#resolve(ElementPath)} that parses the given string before
     * resolution.
     *
     * @param relativePath the relative path string
     * @return a new path representing the combination of this path and another
     */
    @NotNull ElementPath resolve(final @NotNull String relativePath);

    /**
     * Appends some object to this path as a <i>single</i>, new node. If the object is a string, commands will not be
     * interpreted; the value is used as-is for the name of the node only. {@link Objects#toString(Object)} is used to
     * convert arbitrary objects into strings before appending.
     *
     * @param node the object to append
     * @return a new path
     */
    @NotNull ElementPath append(final @Nullable Object node);

    /**
     * Converts this path into an absolute path. Path commands will be removed.
     *
     * @return a new path
     */
    @NotNull ElementPath toAbsolute();

    /**
     * Follows this path through the given {@link ConfigElement} according to its nodes. Node values will be converted
     * to integers, as necessary, if lists are encountered. If this path is relative, it is considered as an absolute
     * path.
     * <p>
     * If the path does not exist, an {@link ElementException} is thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    @NotNull ConfigElement follow(final @NotNull ConfigElement root);

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a {@link ConfigContainer}. If the object is not of the right type, an {@link ElementException} will be
     * thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default @NotNull ConfigContainer followContainer(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isContainer, ConfigElement::asContainer, root, this, "list");
    }

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a {@link ConfigNode}. If the object is not of the right type, an {@link ElementException} will be thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default @NotNull ConfigNode followNode(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isNode, ConfigElement::asNode, root, this, "node");
    }

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a {@link ConfigList}. If the object is not of the right type, an {@link ElementException} will be thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default @NotNull ConfigList followList(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isList, ConfigElement::asList, root, this, "list");
    }

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a scalar (using {@link ConfigElement#asScalar()}). If the object is not of the right type, an
     * {@link ElementException} will be thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default @NotNull Object followScalar(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isScalar, ConfigElement::asScalar, root, this, "scalar");
    }

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a string. If the object is not of the right type, an {@link ElementException} will be thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default @NotNull String followString(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isString, ConfigElement::asString, root, this, "string");
    }

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a number. If the object is not of the right type, an {@link ElementException} will be thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default @NotNull Number followNumber(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isNumber, ConfigElement::asNumber, root, this, "number");
    }

    /**
     * Convenience overload for {@link ElementPath#follow(ConfigElement)} that additionally converts the located element
     * to a boolean. If the object is not of the right type, an {@link ElementException} will be thrown.
     *
     * @param root the root ConfigElement
     * @return the element at the path
     */
    default boolean followBoolean(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isBoolean, ConfigElement::asBoolean, root, this, "boolean");
    }

    /**
     * Indicates various types of nodes.
     */
    enum NodeType {
        /**
         * A node representing the "current" command.
         */
        CURRENT,

        /**
         * A node representing the "previous" command.
         */
        PREVIOUS,

        /**
         * A node representing the name of a particular point along a path.
         */
        NAME
    }

    /**
     * A record representing an individual node in a path.
     *
     * @param name     the name of the node
     * @param nodeType the kind of node this is
     */
    record Node(@NotNull String name, @NotNull NodeType nodeType) {}
}
