package gregtech.common.covers.filter.oreglob.node;

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

    interface Base extends NodeVisitor {

        @Override
        default void match(String match, boolean ignoreCase, boolean not) {}

        @Override
        default void chars(int amount, boolean not) {}

        @Override
        default void charsOrMore(int amount, boolean not) {}

        @Override
        default void group(OreGlobNode node, boolean not) {}

        @Override
        default void branch(BranchType type, List<OreGlobNode> nodes, boolean not) {}

        @Override
        default void everything() {}

        @Override
        default void nothing() {}

        @Override
        default void nonempty() {}

        @Override
        default void empty() {}

        @Override
        default void error() {}
    }
}
