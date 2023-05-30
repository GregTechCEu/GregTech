package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;

class EverythingNode extends OreGlobNode {

    @Override
    public void visit(NodeVisitor visitor) {
        if (isNegated()) visitor.nothing();
        else visitor.everything();
    }

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        return node instanceof EverythingNode;
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return isNegated() ? MatchDescription.NOTHING : MatchDescription.EVERYTHING;
    }
}
