package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;

public class NotNode extends OreGlobNode {

    final OreGlobNode node;

    public NotNode(OreGlobNode node) {
        this.node = node;
    }

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        visitor.not(node);
        return next;
    }
}
