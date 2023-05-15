package gregtech.common.covers.filter.oreglob.node;

import com.google.common.collect.Lists;
import gregtech.common.covers.filter.oreglob.node.BranchNode.BranchType;

import java.util.List;

/**
 * Entry point for accessing all oreGlobNode instances outside package. Thanks to Java for the superior visibility system.
 */
public class OreGlobNodes {

    public static OreGlobNode match(String match) {
        return match(match, true);
    }

    public static OreGlobNode match(String match, boolean ignoreCase) {
        if (match.isEmpty()) return empty();
        return new MatchNode(match, ignoreCase);
    }

    public static OreGlobNode chars(int amount, boolean more) {
        return new AnyCharNode(amount, more);
    }

    public static OreGlobNode group(OreGlobNode node) {
        return new GroupNode(node);
    }

    public static OreGlobNode everything() {
        return chars(0, true);
    }

    public static OreGlobNode nothing() {
        return not(everything());
    }

    public static OreGlobNode something() {
        return chars(1, true);
    }

    public static OreGlobNode empty() {
        return not(something());
    }

    public static OreGlobNode or(OreGlobNode... expressions) {
        return or(Lists.newArrayList(expressions));
    }

    public static OreGlobNode or(List<OreGlobNode> expressions) {
        MatchDescription union = MatchDescription.NOTHING;
        for (int i = 0; i < expressions.size(); i++) {
            OreGlobNode expr = expressions.get(i);
            if (expr.isEverything()) {
                return expr; // short circuit
            } else if (union.covers(expr.getMatchDescription())) {
                expressions.remove(i--); // trivial term
                continue;
            } else if (expr.getMatchDescription().covers(union)) {
                // everything before was trivial
                while (i != 0) {
                    expressions.remove(0);
                    i--;
                }
                union = expr.getMatchDescription();
            } else {
                union = union.or(expr.getMatchDescription());
            }
            if (union == MatchDescription.EVERYTHING) {
                return everything(); // short circuit
            }
        }
        switch (expressions.size()) {
            case 0:
                return nothing();
            case 1:
                return expressions.get(0);
        }
        return new BranchNode(BranchType.OR, expressions);
    }

    public static OreGlobNode and(OreGlobNode... expressions) {
        return and(Lists.newArrayList(expressions));
    }

    public static OreGlobNode and(List<OreGlobNode> expressions) {
        MatchDescription intersection = MatchDescription.EVERYTHING;
        for (int i = 0; i < expressions.size(); i++) {
            OreGlobNode expr = expressions.get(i);
            if (expr.isImpossibleToMatch()) {
                return expr; // short circuit
            } else if (expr.getMatchDescription().covers(intersection)) {
                expressions.remove(i--); // trivial term
                continue;
            } else if (intersection.covers(expr.getMatchDescription())) {
                // everything before was trivial
                while (i != 0) {
                    expressions.remove(0);
                    i--;
                }
                intersection = expr.getMatchDescription();
            } else {
                intersection = intersection.and(expr.getMatchDescription());
            }
            if (intersection == MatchDescription.NOTHING) {
                return nothing(); // short circuit
            }
        }
        switch (expressions.size()) {
            case 0:
                return everything();
            case 1:
                return expressions.get(0);
        }
        return new BranchNode(BranchType.AND, expressions);
    }

    public static OreGlobNode xor(OreGlobNode... expressions) {
        return xor(Lists.newArrayList(expressions));
    }

    public static OreGlobNode xor(List<OreGlobNode> expressions) {
        boolean not = false;
        for (int i1 = 0; i1 < expressions.size(); i1++) {
            OreGlobNode expr = expressions.get(i1);
            if (expr.isImpossibleToMatch()) {
                expressions.remove(i1--); // redundant term
            } else if (expr.isEverything()) {
                expressions.remove(i1--);
                not = !not; // same as applying NOT to every other term
            } else {
                MatchDescription desc = expr.getMatchDescription();
                if (desc.isComplete()) {
                    for (int i2 = 0; i2 < i1; i2++) {
                        MatchDescription desc2 = expressions.get(i2).getMatchDescription();
                        if (desc == desc2) {
                            // both terms cancel each other
                            expressions.remove(i1--);
                            expressions.remove(i2--);
                        } else if (desc.complement() == desc2) {
                            // both terms get combined into true
                            expressions.remove(i1--);
                            expressions.remove(i2--);
                            not = !not;
                        }
                    }
                }
            }
        }
        switch (expressions.size()) {
            case 0:
                return not ? everything() : nothing();
            case 1:
                OreGlobNode node = expressions.get(0);
                return not ? not(node) : node;
        }
        BranchNode node = new BranchNode(BranchType.XOR, expressions);
        return not ? not(node) : node;
    }

    public static OreGlobNode branch(BranchType type, List<OreGlobNode> expressions) {
        switch (type) {
            case OR:
                return or(expressions);
            case AND:
                return and(expressions);
            case XOR:
                return xor(expressions);
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    public static OreGlobNode error() {
        return new ErrorNode();
    }

    public static OreGlobNode not(OreGlobNode node) {
        if (node.hasNext()) {
            GroupNode newNode = new GroupNode(node);
            newNode.setNegated(true);
            return newNode;
        } else {
            node.setNegated(!node.isNegated());
            node.clearMatchDescriptionCache();
            return node;
        }
    }

    public static OreGlobNode append(OreGlobNode node, OreGlobNode next) {
        if (node.isImpossibleToMatch() || next.isNothing()) return node;
        if (next.isImpossibleToMatch() || node.isNothing()) return next;

        if (node instanceof MatchNode) {
            if (next instanceof MatchNode) {
                MatchNode n1 = (MatchNode) node;
                MatchNode n2 = (MatchNode) next;
                if (!node.isNegated() && !next.isNegated()) {
                    if (n1.ignoreCase == n2.ignoreCase) {
                        // two consecutive, non-negated match nodes can be concatenated
                        if (!n2.match.isEmpty()) {
                            n1.match += n2.match;
                            n1.clearMatchDescriptionCache();
                        }
                        n1.setNext(n2.getNext());
                        return n1;
                    }
                } else if (node.isNegated() && next.isNegated()) {
                    if (!n1.match.isEmpty() && !n2.match.isEmpty()) {
                        if (n1.getMatchLength() > 1 || n2.getMatchLength() > 1) {
                            // two consecutive negated matches with more than 1 chars match everything
                            // because logical complements don't care about your feelings
                            return everything();
                        }
                        if (n1.isMatchEquals(n2)) {
                            n1.setNext(n2.getNext());
                            return n1; // (!x) (!x) is equivalent to !x if x is a single char
                        }
                        // turns out (!x) (!y), when both x and y are a single char each, is eq to (!x | !y)
                        OreGlobNode newNode = or(n1, n2);
                        newNode.setNext(n2.getNext());
                        n1.setNext(null);
                        n2.setNext(null);
                        return newNode;
                    }
                }
            }
        } else if (node instanceof AnyCharNode && !node.isNegated()) {
            AnyCharNode n1 = (AnyCharNode) node;
            if (next.isEverything()) {
                if (!n1.more) {
                    n1.more = true;
                    n1.clearMatchDescriptionCache();
                }
                n1.setNext(null);
                return n1;
            } else if (next.isSomething()) {
                n1.amount++;
                if (!n1.more) {
                    n1.more = true;
                    n1.clearMatchDescriptionCache();
                }
                n1.clearMatchDescriptionCache();
                n1.setNext(null);
                return n1;
            }

            if (next instanceof AnyCharNode && !next.isNegated()) {
                AnyCharNode n2 = (AnyCharNode) next;
                // two consecutive, non-negated char nodes can be concatenated
                n1.amount += n2.amount;
                n1.more |= n2.more;
                n1.setNext(n2.getNext());
                n1.clearMatchDescriptionCache();
                return n1;
            }
        }

        node.setNext(next);
        return node;
    }

    public static OreGlobNode append(OreGlobNode... nodes) {
        if (nodes.length == 0) throw new IllegalArgumentException("No nodes provided");
        for (int i = nodes.length - 2; i >= 0; i--) {
            nodes[i] = append(nodes[i], nodes[i + 1]);
        }
        return nodes[0];
    }
}
