package com.github.steanky.element.core.path;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

public interface ElementPath {
    ElementPath EMPTY = BasicElementPath.EMPTY_PATH;

    Node CURRENT_NODE = new Node(".", NodeType.CURRENT);
    Node PREVIOUS_NODE = new Node("..", NodeType.PREVIOUS);

    record Node(@NotNull String name, @NotNull NodeType nodeType) {}

    enum NodeType {
        CURRENT,
        PREVIOUS,
        NAME
    }

    @NotNull @Unmodifiable List<Node> nodes();

    boolean isAbsolute();

    @NotNull ElementPath resolve(final @NotNull ElementPath relativePath);

    @NotNull ElementPath resolve(final @NotNull String relativePath);

    @NotNull ElementPath append(final @Nullable Object node);

    @NotNull ConfigElement follow(final @NotNull ConfigElement root);

    default @NotNull ConfigContainer followContainer(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isContainer, ConfigElement::asContainer, root,
                this, "list");
    }

    default @NotNull ConfigNode followNode(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isNode, ConfigElement::asNode, root, this,
                "node");
    }

    default @NotNull ConfigList followList(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isList, ConfigElement::asList, root, this,
                "list");
    }

    default @NotNull Object followScalar(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isScalar, ConfigElement::asScalar, root, this,
                "scalar");
    }

    default @NotNull String followString(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isString, ConfigElement::asString, root, this,
                "string");
    }

    default @NotNull Number followNumber(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isNumber, ConfigElement::asNumber, root, this,
                "number");
    }

    default boolean followBoolean(final @NotNull ConfigElement root) {
        return ElementPathUtils.follow(ConfigElement::isBoolean, ConfigElement::asBoolean, root, this,
                "boolean");
    }

    static @NotNull ElementPath of(final @NotNull String path) {
        Objects.requireNonNull(path);
        return BasicElementPath.parse(path);
    }
}
