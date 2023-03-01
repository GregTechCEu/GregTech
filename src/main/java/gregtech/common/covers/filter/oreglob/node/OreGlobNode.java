package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;

/**
 * Syntax tree representation of ore expression.
 */
public abstract class OreGlobNode {

    @Nullable
    OreGlobNode next;
    boolean inverted;

    // package-private constructor to prevent inheritance from outside
    OreGlobNode() {
    }

    /**
     * Visit this node.
     *
     * @param visitor Visitor for this node
     * @return Next node, if exists
     */
    @Nullable
    public abstract OreGlobNode visit(NodeVisitor visitor);
}

