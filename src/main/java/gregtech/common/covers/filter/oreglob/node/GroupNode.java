package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

class GroupNode extends OreGlobNode {

    final OreGlobNode node;

    GroupNode(OreGlobNode node) {
        this.node = node;
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.group(node, isNegated());
    }

    @Override
    public boolean isPropertyEqualTo(@NotNull OreGlobNode node) {
        return node instanceof GroupNode groupNode && this.node.isStructurallyEqualTo(groupNode.node);
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return isNegated() ? node.getMatchDescription().complement() : node.getMatchDescription();
    }
}
