package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;

import java.util.List;

class BranchNode extends OreGlobNode {

    final BranchType type;
    final List<OreGlobNode> expressions;

    BranchNode(BranchType type, List<OreGlobNode> expressions) {
        this.type = type;
        this.expressions = expressions;
    }

    @Override
    public void visit(NodeVisitor visitor) {
        if (expressions.size() == 1) {
            visitor.group(expressions.get(0), isNegated());
        } else {
            visitor.branch(type, expressions, isNegated());
        }
    }

    @Override
    protected MatchDescription getIndividualNodeMatchDescription() {
        MatchDescription description = switch (this.expressions.size()) {
            case 0 -> this.type == BranchType.AND ? MatchDescription.EVERYTHING : MatchDescription.NOTHING;
            case 1 -> this.expressions.get(0).getMatchDescription();
            default -> switch (this.type) {
                    case OR -> {
                        MatchDescription union = MatchDescription.NOTHING;
                        for (OreGlobNode node : this.expressions) {
                            union = union.or(node.getMatchDescription());
                        }
                        yield union;
                    }
                    case AND -> {
                        MatchDescription intersection = MatchDescription.EVERYTHING;
                        for (OreGlobNode node : this.expressions) {
                            intersection = intersection.and(node.getMatchDescription());
                        }
                        yield intersection;
                    }
                    case XOR -> {
                        MatchDescription disjunction = MatchDescription.NOTHING;
                        for (OreGlobNode node : this.expressions) {
                            disjunction = disjunction.xor(node.getMatchDescription());
                        }
                        yield disjunction;
                    }
                };
        };
        return isNegated() ? description.complement() : description;
    }

    @Override
    public boolean isPropertyEqualTo(@NotNull OreGlobNode node) {
        if (!(node instanceof BranchNode br)) return false;
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
}
