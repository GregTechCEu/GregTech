package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;

public class GroupNode extends OreGlobNode {

    final OreGlobNode node;

    public GroupNode(OreGlobNode node) {
        this.node = node;
    }

    @Override
    protected void visitInternal(NodeVisitor visitor) {
        visitor.group(node, inverted);
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
