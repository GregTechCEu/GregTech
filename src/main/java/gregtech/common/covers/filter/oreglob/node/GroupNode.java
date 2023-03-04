package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;

public class GroupNode extends OreGlobNode {

    final OreGlobNode node;

    public GroupNode(OreGlobNode node) {
        this.node = node;
    }

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        visitor.group(node, inverted);
        return next;
    }
}
