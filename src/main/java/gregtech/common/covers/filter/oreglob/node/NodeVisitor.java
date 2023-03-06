package gregtech.common.covers.filter.oreglob.node;

import java.util.List;

public interface NodeVisitor {

    void match(String match, boolean ignoreCase, boolean inverted);

    void chars(int amount, boolean inverted);

    void charsOrMore(int amount, boolean inverted);

    void group(OreGlobNode node, boolean inverted);

    void branch(BranchType type, List<OreGlobNode> nodes, boolean inverted);

    void error();
}
