package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;

class EmptyNode extends OreGlobNode {

    @Override
    public void visit(NodeVisitor visitor) {
        if (isNegated()) visitor.nonempty();
        else visitor.empty();
    }

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        return node instanceof EmptyNode;
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return isNegated() ? MatchDescription.NONEMPTY : MatchDescription.EMPTY;
    }
}
