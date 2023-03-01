package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.BranchType;
import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;
import java.util.List;

class BranchNode extends OreGlobNode {

    final BranchType type;
    final List<OreGlobNode> expressions;

    BranchNode(BranchType type, List<OreGlobNode> expressions) {
        this.type = type;
        this.expressions = expressions;
    }

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        visitor.branch(type, expressions, inverted);
        return next;
    }
}
