package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;

public class AnyCharNode extends OreGlobNode {

    int amount;
    boolean more;

    AnyCharNode(int amount, boolean more) {
        this.amount = amount;
        this.more = more;
    }

    @Override
    protected void visitInternal(NodeVisitor visitor) {
        if (more) {
            visitor.charsOrMore(amount, isNegated());
        } else {
            visitor.chars(amount, isNegated());
        }
    }

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        if (!(node instanceof AnyCharNode)) return false;
        AnyCharNode o = (AnyCharNode) node;
        return this.amount == o.amount && this.more == o.more;
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        if (this.more) {
            if (this.amount == 0) return MatchDescription.EVERYTHING;
            if (this.amount == 1) return MatchDescription.NONEMPTY;
        } else {
            if (this.amount == 0) return MatchDescription.EMPTY;
        }
        return MatchDescription.OTHER_EXCLUDING_EMPTY;
    }
}
