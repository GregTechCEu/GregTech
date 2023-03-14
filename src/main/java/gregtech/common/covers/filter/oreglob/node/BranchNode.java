package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;
import java.util.List;

public class BranchNode extends OreGlobNode {

    final BranchType type;
    final List<OreGlobNode> expressions;

    BranchNode(BranchType type, List<OreGlobNode> expressions) {
        this.type = type;
        this.expressions = expressions;
    }

    @Override
    protected void visitInternal(NodeVisitor visitor) {
        if (expressions.size() == 1) {
            visitor.group(expressions.get(0), isNegated());
        } else {
            visitor.branch(type, expressions, isNegated());
        }
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        switch (this.expressions.size()) {
            case 0:
                return this.type == BranchType.AND ? MatchDescription.EVERYTHING : MatchDescription.NOTHING;
            case 1:
                return this.expressions.get(0).getIndividualNodeMatchDescription();
        }
        switch (this.type) {
            case OR: {
                MatchDescription union = MatchDescription.NOTHING;
                for (OreGlobNode node : this.expressions) {
                    MatchDescription desc = node.getMatchDescription();
                    if (desc == MatchDescription.NOTHING) continue;
                    union = union.or(desc);
                    if (union == MatchDescription.EVERYTHING) {
                        return MatchDescription.EVERYTHING;
                    }
                }
                return union;
            }
            case AND: {
                MatchDescription intersection = MatchDescription.EVERYTHING;
                for (OreGlobNode node : this.expressions) {
                    MatchDescription desc = node.getMatchDescription();
                    if (desc == MatchDescription.EVERYTHING) continue;
                    intersection = intersection.and(desc);
                    if (intersection == MatchDescription.EMPTY) {
                        return MatchDescription.EMPTY;
                    }
                }
                return intersection;
            }
            case XOR: {
                MatchDescription disjunction = MatchDescription.NOTHING;
                for (OreGlobNode node : this.expressions) {
                    MatchDescription desc = node.getMatchDescription();
                    disjunction = disjunction.xor(desc);
                }
                return disjunction;
            }
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    @Override
    public boolean isPropertyEqualTo(@Nonnull OreGlobNode node) {
        if (!(node instanceof BranchNode)) return false;
        BranchNode br = (BranchNode) node;
        if (this.type != br.type) return false;
        if (this.expressions.size() != br.expressions.size()) return false;

        // since all three operations (OR, AND, XOR) are both commutative and associative,
        // we should be able to match all elements with no regards to the index
        boolean[] matchFlag = new boolean[this.expressions.size()];
        for (OreGlobNode node1 : this.expressions) {
            boolean matched = false;
            for (int i2 = 0; i2 < br.expressions.size(); i2++) {
                if (matchFlag[i2]) continue;
                if (node1.isStructurallyEqualTo(br.expressions.get(i2))) {
                    matchFlag[i2] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return true;
    }

    public enum BranchType {
        OR, AND, XOR
    }
}
