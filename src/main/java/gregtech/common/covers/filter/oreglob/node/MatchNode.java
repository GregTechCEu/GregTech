package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class MatchNode extends OreGlobNode {

    String match;
    boolean ignoreCase;

    MatchNode(String match, boolean ignoreCase) {
        this.match = match;
        this.ignoreCase = ignoreCase;
    }

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        visitor.match(match, ignoreCase, inverted);
        return next;
    }

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        if (!(node instanceof MatchNode)) return false;
        String match = ((MatchNode) node).match;
        return this.ignoreCase ? this.match.equalsIgnoreCase(match) : this.match.equals(match);
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return this.match.isEmpty() ? MatchDescription.NOTHING : MatchDescription.OTHER_EXCLUDING_NOTHING;
    }
}
