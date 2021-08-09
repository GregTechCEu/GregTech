package gregtech.api.pipenet;

import net.minecraft.util.EnumFacing;

/**
 * Represents a single node in network of pipes
 * It can have blocked connections and be active or not
 */
public final class Node<NodeDataType> {

    public static final int DEFAULT_MARK = 0;

    public final NodeDataType data;
    /**
     * Specifies bitmask of blocked connections
     * Node will not connect in blocked direction in any case,
     * even if neighbour node mark matches
     */
    public int openConnections;
    /**
     * Specifies mark of this node
     * Nodes can connect only if their marks are equal, or if
     * one of marks is default one
     */
    public int mark;
    public boolean isActive;

    public Node(NodeDataType data, int openConnections, int mark, boolean isActive) {
        this.data = data;
        this.openConnections = openConnections;
        this.mark = mark;
        this.isActive = isActive;
    }

    public boolean isBlocked(EnumFacing facing) {
        return (openConnections & 1 << facing.getIndex()) == 0;
    }
}
