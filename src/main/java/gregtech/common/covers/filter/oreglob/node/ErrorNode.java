package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;

class ErrorNode extends OreGlobNode {

    @Override
    protected void visitInternal(NodeVisitor visitor) {
        visitor.error();
    }

    @Override
    public boolean isStructurallyEqualTo(@Nonnull OreGlobNode node) { // removed inverted flag check
        return this == node || node instanceof ErrorNode && isStructurallyEqualTo(this.next, node.next);
    }

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        return node instanceof ErrorNode;
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return MatchDescription.IMPOSSIBLE;
    }
}
