package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

class EmptyNode extends OreGlobNode {

    @Override
    public void visit(NodeVisitor visitor) {
        if (isNegated()) visitor.nonempty();
        else visitor.empty();
    }

    @Override
    public boolean isPropertyEqualTo(@NotNull OreGlobNode node) {
        return node instanceof EmptyNode;
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return isNegated() ? MatchDescription.NONEMPTY : MatchDescription.EMPTY;
    }
}
