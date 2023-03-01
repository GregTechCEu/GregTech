package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.NodeVisitor;

import javax.annotation.Nullable;

class ErrorNode extends OreGlobNode {

    @Nullable
    @Override
    public OreGlobNode visit(NodeVisitor visitor) {
        visitor.error();
        return next;
    }
}
