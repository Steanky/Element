package com.github.steanky.element.core.path;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.toolkit.collection.Containers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.github.steanky.element.core.util.Validate.elementException;

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
    static final BasicElementPath RELATIVE_BASE = new BasicElementPath(new Node[]{CURRENT_NODE});
    static final BasicElementPath PREVIOUS_BASE = new BasicElementPath(new Node[]{PREVIOUS_NODE});

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
        if (path.isEmpty()) {
            return EMPTY_PATH;
        }

        if (path.equals(".")) {
            return RELATIVE_BASE;
        }

        if(path.equals("..")) {
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

        if (relativeNodes.isEmpty()) {
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
                    if (!newNodes.isEmpty() && newNodes.peekLast().nodeType() != NodeType.PREVIOUS) {
                        newNodes.removeLast();
                    }
                    else {
                        newNodes.addLast(node);
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

        int i;
        for (i = 0; i < nodes.length; i++) {
            if (nodes[i].nodeType() == NodeType.NAME) {
                break;
            }
        }

        if (i == nodes.length) {
            return EMPTY_PATH;
        }

        final Node[] newNodes = new Node[nodes.length - i];
        System.arraycopy(this.nodes, i, newNodes, 0, newNodes.length);
        return new BasicElementPath(newNodes);
    }

    @Override
    public ElementPath getParent() {
        if (nodes.length == 0) {
            return null;
        }

        final Node[] newNodes = new Node[nodes.length - 1];
        System.arraycopy(nodes, 0, newNodes, 0, newNodes.length);
        return new BasicElementPath(newNodes);
    }

    @Override
    public @NotNull ElementPath relativize(final @NotNull ElementPath other) {
        if (this.equals(other)) {
            return RELATIVE_BASE;
        }

        if (this.isAbsolute() != other.isAbsolute()) {
            throw new IllegalArgumentException("Can only relativize paths of the same type");
        }

        final Node[] otherNodes = nodeArray(other);
        final int min = Math.min(otherNodes.length, this.nodes.length);
        final int max = Math.max(otherNodes.length, this.nodes.length);

        int matched = matched(this, this.nodes, other, otherNodes, min);

        if (otherNodes.length < this.nodes.length || matched < min) {
            final int unmatched = min - matched;
            final int previousCount = max - matched;
            final Node[] newNodes = new Node[previousCount + unmatched];

            Arrays.fill(newNodes, 0, previousCount, PREVIOUS_NODE);
            System.arraycopy(otherNodes, matched, newNodes, previousCount, unmatched);
            return new BasicElementPath(newNodes);
        }

        //matched == min && otherNodes.length >= this.nodes.length
        final int unmatched = max - min;
        final Node[] newNodes = new Node[unmatched + 1];
        newNodes[0] = CURRENT_NODE;
        System.arraycopy(otherNodes, matched, newNodes, 1, unmatched);
        return new BasicElementPath(newNodes);

    }

    private static int matched(final ElementPath self, final Node[] selfNodes, final ElementPath other,
            final Node[] otherNodes, final int min) {
        int matched = 0;

        //count number of shared elements
        while (matched < min) {
            if (!otherNodes[matched].equals(selfNodes[matched])) {
                break;
            }

            matched++;
        }

        //remaining .. in this path means we have no sane way to resolve the relative path
        for (int i = matched; i < selfNodes.length; i++) {
            if (selfNodes[i].nodeType() == NodeType.PREVIOUS) {
                throw new IllegalArgumentException("Cannot compute relative path from " + self + " to " + other);
            }
        }

        return matched;
    }

    @Override
    public @NotNull ElementPath relativize(final @NotNull String other) {
        return relativize(parse(other));
    }

    @Override
    public @NotNull ElementPath resolveSibling(final @NotNull ElementPath sibling) {
        if (sibling.isAbsolute()) {
            return sibling;
        }

        final ElementPath parentPath = getParent();
        if (parentPath == null) {
            return sibling;
        }

        return parentPath.resolve(sibling);
    }

    @Override
    public @NotNull ElementPath resolveSibling(final @NotNull String sibling) {
        return resolveSibling(parse(sibling));
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
                        formatPathException("Index " + value + " out of bounds for ConfigList of length " + size, i);
                    }

                    current = list.get(value);
                } catch (NumberFormatException e) {
                    formatPathException("String " + name + " cannot be parsed", i);
                }
            } else {
                formatPathException("Unexpected ConfigElement type " + current.type(), i);
            }

            if (current == null) {
                formatPathException("Does not exist", i);
            }
        }

        return current;
    }

    @Override
    public @NotNull ElementPath subpath(final int beginIndex, final int endIndex) {
        if (beginIndex < 0 || beginIndex >= nodes.length || endIndex < 0 || endIndex > nodes.length) {
            throw new IndexOutOfBoundsException();
        }

        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex");
        }

        final int length = endIndex - beginIndex;
        if (nodes[beginIndex].nodeType() != NodeType.NAME) {
            final Node[] newNodes = new Node[length];
            System.arraycopy(nodes, beginIndex, newNodes, 0, length);
            return new BasicElementPath(newNodes);
        }

        final Node[] newNodes = new Node[length + 1];
        newNodes[0] = CURRENT_NODE;
        System.arraycopy(nodes, 0, newNodes, 1, length);
        return new BasicElementPath(newNodes);
    }

    @Override
    public boolean startsWith(final @NotNull ElementPath other) {
        if (this.nodes.length == 0) {
            return other.nodes().isEmpty();
        }

        final List<Node> otherNodes = other.nodes();
        if (otherNodes.size() > this.nodes.length) {
            return false;
        }

        for (int i = 0; i < otherNodes.size(); i++) {
            if (!this.nodes[i].equals(otherNodes.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static Node[] nodeArray(ElementPath elementPath) {
        if (elementPath instanceof BasicElementPath basicElementPath) {
            return basicElementPath.nodes;
        }

        return elementPath.nodes().toArray(Node[]::new);
    }

    private void formatPathException(final String message, final int position) {
        throw elementException(this, "Path element " + nodes[position].name() + ": " + message);
    }

    @Override
    public int hashCode() {
        if (hashed) {
            return hash;
        }

        final int hash = this.hash = Arrays.hashCode(nodes);
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
