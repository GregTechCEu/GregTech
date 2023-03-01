package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;

class MatchNode extends OreGlobNode {

    final String match;

    MatchNode(String match) {
        this.match = match;
    }

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        visitor.match(match, inverted);
        return next;
    }
}
