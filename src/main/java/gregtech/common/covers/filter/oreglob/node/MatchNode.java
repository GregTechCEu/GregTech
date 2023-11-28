package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

class MatchNode extends OreGlobNode {

    String match;
    boolean ignoreCase;

    MatchNode(String match, boolean ignoreCase) {
        this.match = match;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.match(match, ignoreCase, isNegated());
    }

    @Override
    public boolean isPropertyEqualTo(@NotNull OreGlobNode node) {
        return node instanceof MatchNode match &&
                (this.ignoreCase ? this.match.equalsIgnoreCase(match.match) : this.match.equals(match.match));
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        return isNegated() ? MatchDescription.OTHER_INCLUDING_EMPTY : MatchDescription.OTHER_EXCLUDING_EMPTY;
    }

    public boolean isMatchEquals(MatchNode other) {
        return this.ignoreCase ? this.match.equalsIgnoreCase(other.match) : this.match.equals(other.match);
    }

    public int getMatchLength() {
        return this.match.codePointCount(0, this.match.length());
    }
}
