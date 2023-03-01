package gregtech.common.covers.filter.oreglob.node;

import gregtech.common.covers.filter.oreglob.BranchType;

import java.util.List;

/**
 * Entry point for accessing all oreGlobNode instances outside package. Thanks to Java for the superior visibility system.
 */
public class OreGlobNodes {

    public static OreGlobNode match(String match) {
        return new MatchNode(match);
    }

    public static OreGlobNode chars(int amount, boolean more) {
        return new AnyCharNode(amount, more);
    }

    public static OreGlobNode not(OreGlobNode node) {
        return new NotNode(node);
    }

    public static OreGlobNode nothing() {
        OreGlobNode node = chars(1, true);
        node.inverted = true;
        return node;
    }

    public static OreGlobNode or(List<OreGlobNode> expressions) {
        return new BranchNode(BranchType.OR, expressions);
    }

    public static OreGlobNode and(List<OreGlobNode> expressions) {
        return new BranchNode(BranchType.AND, expressions);
    }

    public static OreGlobNode xor(List<OreGlobNode> expressions) {
        return new BranchNode(BranchType.XOR, expressions);
    }

    public static OreGlobNode error() {
        return new ErrorNode();
    }

    public static void invert(OreGlobNode node) {
        node.inverted = !node.inverted;
    }

    public static OreGlobNode setNext(OreGlobNode node, OreGlobNode next) {
        // TODO FUCK
        if (node instanceof MatchNode) {
            if (!node.inverted && !next.inverted && next instanceof MatchNode) {
                // two consecutive, non-inverted match nodes can be concatenated
                MatchNode newNode = new MatchNode(((MatchNode) node).match + ((MatchNode) next).match);
                newNode.next = next.next;
                return newNode;
            }
        } else if (node instanceof AnyCharNode) {
            if (!node.inverted && !next.inverted && next instanceof AnyCharNode) {
                // two consecutive, non-inverted char nodes can be concatenated
                // uh maybe inverted nodes too?? I can't think right now fuck
                AnyCharNode n1 = (AnyCharNode) node;
                AnyCharNode n2 = (AnyCharNode) next;
                AnyCharNode newNode = new AnyCharNode(n1.amount + n2.amount, n1.more || n2.more);
                newNode.next = next.next;
                return newNode;
            }
        }

        node.next = next;
        return node;
    }
}
