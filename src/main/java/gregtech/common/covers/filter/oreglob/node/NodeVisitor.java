package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.node.BranchNode.BranchType;

import java.util.List;

public interface NodeVisitor {

    void match(String match, boolean ignoreCase, boolean not);

    void chars(int amount, boolean not);

    void charsOrMore(int amount, boolean not);

    void group(OreGlobNode node, boolean not);

    void branch(BranchType type, List<OreGlobNode> nodes, boolean not);

    void everything();

    void nothing();

    void nonempty();

    void empty();

    void error();
}
