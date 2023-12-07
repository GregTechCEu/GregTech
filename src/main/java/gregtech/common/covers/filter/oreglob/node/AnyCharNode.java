package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

class AnyCharNode extends OreGlobNode {

    int amount;
    boolean more;

    AnyCharNode(int amount, boolean more) {
        this.amount = amount;
        this.more = more;
    }

    @Override
    public void visit(NodeVisitor visitor) {
        if (more) {
            visitor.charsOrMore(amount, isNegated());
        } else {
            visitor.chars(amount, isNegated());
        }
    }

    @Override
    public boolean isPropertyEqualTo(@NotNull OreGlobNode node) {
        return node instanceof AnyCharNode o && this.amount == o.amount && this.more == o.more;
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return isNegated() ? MatchDescription.OTHER_INCLUDING_EMPTY : MatchDescription.OTHER_EXCLUDING_EMPTY;
    }
}
