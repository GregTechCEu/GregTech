package gregtech.common.covers.filter.oreglob;

import gregtech.common.covers.filter.oreglob.node.OreGlobNode;

import java.util.List;

public interface NodeVisitor {
    void match(String match, boolean inverted);

    void chars(int amount, boolean inverted);
    void charsOrMore(int amount, boolean inverted);

    void not(OreGlobNode node);

    void branch(BranchType type, List<OreGlobNode> nodes, boolean inverted);

    void error();
}
