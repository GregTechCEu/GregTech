package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;

class AnyCharNode extends OreGlobNode {

    final int amount;
    final boolean more;

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
}
