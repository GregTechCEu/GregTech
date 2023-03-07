package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;

public class MatchNode extends OreGlobNode {

    String match;
    boolean ignoreCase;

    MatchNode(String match, boolean ignoreCase) {
        this.match = match;
        this.ignoreCase = ignoreCase;
    }

    @Override
    protected void visitInternal(NodeVisitor visitor) {
        visitor.match(match, ignoreCase, isInverted());
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

    public boolean isMatchEquals(MatchNode other) {
        return this.ignoreCase ? this.match.equalsIgnoreCase(other.match) : this.match.equals(other.match);
    }

    public int getMatchLength() {
        return this.match.codePointCount(0, this.match.length());
    }
}
