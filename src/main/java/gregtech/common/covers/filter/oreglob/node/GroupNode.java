package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nonnull;
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

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        if (!(node instanceof GroupNode)) return false;
        return this.node.isStructurallyEqualTo(((GroupNode) node).node);
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return node.getMatchDescription();
    }
}
