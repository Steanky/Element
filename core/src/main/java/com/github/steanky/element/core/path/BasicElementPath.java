package com.github.steanky.element.core.path;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.toolkit.collection.Containers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Basic ElementPath implementation with UNIX-like semantics.
 */
class BasicElementPath implements ElementPath {
    private static final String CURRENT_COMMAND = ".";
    private static final String PREVIOUS_COMMAND = "..";

    private static final Node CURRENT_NODE = new Node(CURRENT_COMMAND, NodeType.CURRENT);
    private static final Node PREVIOUS_NODE = new Node(PREVIOUS_COMMAND, NodeType.PREVIOUS);

    private static final Node[] EMPTY_NODE_ARRAY = new Node[0];
    static final BasicElementPath EMPTY_PATH = new BasicElementPath(EMPTY_NODE_ARRAY);
    private static final BasicElementPath RELATIVE_BASE = new BasicElementPath(new Node[]{CURRENT_NODE});
    private static final BasicElementPath PREVIOUS_BASE = new BasicElementPath(new Node[]{PREVIOUS_NODE});

    private static final char DELIMITER = '/';
    private static final char ESCAPE = '\\';

    private static final char CURRENT = '.';

    private final Node[] nodes;
    private final List<Node> nodeView;

    private int hash;
    private boolean hashed;

    private String stringValue;

    private BasicElementPath(final @NotNull Node @NotNull [] nodeArray) {
        this.nodes = nodeArray;
        this.nodeView = Containers.arrayView(nodeArray);
    }

    private static boolean isCharacterEscapable(char current) {
        return current == DELIMITER || current == CURRENT || current == ESCAPE;
    }

    /**
     * Parses the given UNIX-style path string into a {@link BasicElementPath}. The resulting object will represent the
     * normalized path, with redundant elements removed.
     *
     * @param path the path string
     * @return a normalized BasicElementPath
     */
    static @NotNull BasicElementPath parse(final @NotNull String path) {
        if (path.equals(".")) {
            return RELATIVE_BASE;
        }
        else if(path.equals("..")) {
            return PREVIOUS_BASE;
        }

        final int pathLength = path.length();

        final List<Node> nodes = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();

        boolean escape = false;
        boolean nodeEscape = false;
        for (int i = 0; i < pathLength; i++) {
            final char current = path.charAt(i);

            if (escape) {
                if (!isCharacterEscapable(current)) {
                    //re-add the escape character that we previously encountered
                    builder.append(ESCAPE);
                }

                builder.append(current);
                escape = false;
            } else if (current == ESCAPE) {
                escape = true;

                if (builder.isEmpty()) {
                    nodeEscape = true;
                }
            } else if (current == DELIMITER) {
                tryAddNode(nodes, builder, nodeEscape);
                nodeEscape = false;
            } else {
                builder.append(current);
            }
        }

        tryAddNode(nodes, builder, nodeEscape);

        if (nodes.isEmpty()) {
            return BasicElementPath.EMPTY_PATH;
        }

        //process the path to remove unnecessary or redundant PREVIOUS commands
        //we don't have to worry about redundant CURRENTs because they wouldn't have been added in the first place
        for (int i = nodes.size() - 1; i > 0; i--) {
            final Node node = nodes.get(i);
            if (node.nodeType() != NodeType.PREVIOUS) {
                continue;
            }

            final int previousIndex = i - 1;
            final Node previous = nodes.get(previousIndex);
            final NodeType previousType = previous.nodeType();
            if (previousType == NodeType.PREVIOUS) {
                //don't remove the previous node if it's also a previous
                continue;
            }

            //the current PREVIOUS command erased the previous node
            nodes.remove(previousIndex);

            if (previousType == NodeType.NAME) {
                //strip out redundant PREVIOUS commands, otherwise leave them alone
                nodes.remove(previousIndex);
            }

            if (i > nodes.size()) {
                i--;
            }
        }

        if (nodes.isEmpty()) {
            return EMPTY_PATH;
        }

        return new BasicElementPath(nodes.toArray(Node[]::new));
    }

    private static void tryAddNode(final List<Node> nodes, final StringBuilder builder, final boolean escape) {
        if (builder.isEmpty()) {
            return;
        }

        final String string = builder.toString();
        final boolean empty = nodes.isEmpty();
        if (!empty && !escape && string.length() <= 1 && string.charAt(0) == CURRENT) {
            builder.setLength(0);
            return;
        }

        final NodeType type = escape ? NodeType.NAME : switch (string) {
            case PREVIOUS_COMMAND -> NodeType.PREVIOUS;
            case CURRENT_COMMAND -> NodeType.CURRENT;
            default -> NodeType.NAME;
        };

        nodes.add(switch (type) {
            case CURRENT -> CURRENT_NODE;
            case PREVIOUS -> PREVIOUS_NODE;
            case NAME -> new Node(string, NodeType.NAME);
        });

        builder.setLength(0);
    }

    @Override
    public @NotNull @Unmodifiable List<Node> nodes() {
        return nodeView;
    }

    @Override
    public boolean isAbsolute() {
        return nodes.length == 0 || nodes[0].nodeType() == NodeType.NAME;
    }

    @Override
    public @NotNull ElementPath resolve(final @NotNull ElementPath relativePath) {
        if (relativePath.isAbsolute()) {
            //mimic behavior of Path#resolve(Path)
            return relativePath;
        }

        final List<Node> ourNodes = nodes();
        final List<Node> relativeNodes = relativePath.nodes();

        if (relativeNodes.size() == 0) {
            return this;
        }

        final Deque<Node> newNodes = new ArrayDeque<>(ourNodes.size() + relativeNodes.size());

        for (final Node node : ourNodes) {
            newNodes.addLast(node);
        }

        for (final Node node : relativeNodes) {
            switch (node.nodeType()) {
                case NAME -> newNodes.addLast(node);
                case CURRENT -> {
                } //no-op
                case PREVIOUS -> { //resolve previous command
                    if (!newNodes.isEmpty()) {
                        newNodes.removeLast();
                    }
                }
            }
        }

        if (newNodes.isEmpty()) {
            return EMPTY_PATH;
        }

        return new BasicElementPath(newNodes.toArray(Node[]::new));
    }

    @Override
    public @NotNull ElementPath resolve(@NotNull String relativePath) {
        return resolve(parse(relativePath));
    }

    @Override
    public @NotNull ElementPath append(@Nullable Object node) {
        final Node[] newNodes = new Node[nodes.length + 1];
        System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
        newNodes[newNodes.length - 1] = new Node(Objects.toString(node), NodeType.NAME);
        return new BasicElementPath(newNodes);
    }

    @Override
    public @NotNull ElementPath toAbsolute() {
        if (isAbsolute()) {
            return this;
        }

        final List<Node> newNodes = new ArrayList<>(nodes.length - 1);
        for (final Node node : nodes) {
            if (node.nodeType() == NodeType.NAME) {
                newNodes.add(node);
            }
        }

        return new BasicElementPath(newNodes.toArray(Node[]::new));
    }

    @Override
    public @NotNull ConfigElement follow(final @NotNull ConfigElement root) {
        Objects.requireNonNull(root);
        ConfigElement current = root;

        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];

            final NodeType type = node.nodeType();
            if (type == NodeType.CURRENT || type == NodeType.PREVIOUS) {
                continue;
            }

            final String name = node.name();
            if (current.isNode()) {
                current = current.asNode().get(name);
            } else if (current.isList()) {
                final ConfigList list = current.asList();
                final int size = list.size();

                try {

                    final int value = Integer.parseInt(name);
                    if (value < 0 || value > size) {
                        formatPathException("index " + value + " out of bounds for ConfigList of length " + size, i);
                    }

                    current = list.get(value);
                } catch (NumberFormatException e) {
                    formatPathException("string " + name + " cannot be parsed", i);
                }
            } else {
                formatPathException("unexpected ConfigElement type " + current.type(), i);
            }

            if (current == null) {
                formatPathException("target element does not exist", i);
            }
        }

        return current;
    }

    private void formatPathException(String message, int position) {
        throw new ElementException(this + ", position " + position + ": " + message);
    }

    @Override
    public int hashCode() {
        if (hashed) {
            return hash;
        }

        int hash = this.hash = Arrays.hashCode(nodes);
        hashed = true;
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof ElementPath elementPath) {
            if (obj instanceof BasicElementPath basicElementPath) {
                //do array equality comparison if we can
                return Arrays.equals(nodes, basicElementPath.nodes);
            }

            return nodes().equals(elementPath.nodes());
        }

        return false;
    }

    @Override
    public String toString() {
        return Objects.requireNonNullElseGet(stringValue,
                () -> stringValue = String.join("/", Arrays.stream(nodes).map(Node::name).toArray(String[]::new)));
    }
}
