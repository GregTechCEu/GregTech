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

    public static OreGlobNode group(OreGlobNode node) {
        return new GroupNode(node);
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

    public static OreGlobNode invert(OreGlobNode node) {
        if (node.next != null) {
            GroupNode newNode = new GroupNode(node);
            newNode.inverted = true;
            return newNode;
        } else {
            node.inverted = !node.inverted;
            return node;
        }
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
            AnyCharNode n1 = (AnyCharNode) node;
            if (isImpossibleToMatch(n1)) return node; // Concatenating impossible case
            if (isNothing(n1)) return next; // Nothing does nothing, literally
            if (!node.inverted && next instanceof AnyCharNode && !next.inverted) {
                AnyCharNode n2 = (AnyCharNode) next;
                // two consecutive, non-inverted char nodes can be concatenated
                // uh maybe inverted nodes too?? I can't think right now fuck
                AnyCharNode newNode = new AnyCharNode(n1.amount + n2.amount, n1.more || n2.more);
                newNode.next = next.next;
                return newNode;
            }
        }
        if (next instanceof AnyCharNode) {
            AnyCharNode n2 = (AnyCharNode) next;
            if (isImpossibleToMatch(n2)) return next; // Concatenating impossible case
            if (isNothing(n2)) return node; // Nothing does nothing, literally
        }

        node.next = next;
        return node;
    }

    private static boolean isImpossibleToMatch(AnyCharNode node) {
        return node.inverted && node.more && node.amount == 0;
    }

    private static boolean isNothing(AnyCharNode node) {
        return node.inverted && node.more && node.amount == 1;
    }
}
