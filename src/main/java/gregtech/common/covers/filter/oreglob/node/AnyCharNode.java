package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnyCharNode extends OreGlobNode {

    int amount;
    boolean more;

    public AnyCharNode(int amount, boolean more) {
        this.amount = amount;
        this.more = more;
    }

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        if (more) {
            visitor.charsOrMore(amount, inverted);
        } else {
            visitor.chars(amount, inverted);
        }
        return next;
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
            if (this.amount == 1) return MatchDescription.SOMETHING;
        } else {
            if (this.amount == 0) return MatchDescription.NOTHING;
        }
        return MatchDescription.OTHER_EXCLUDING_NOTHING;
    }
}
