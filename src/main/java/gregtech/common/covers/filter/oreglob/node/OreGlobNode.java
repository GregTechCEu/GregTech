package gregtech.common.covers.filter.oreglob.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Syntax tree representation of ore expression.
 */
public abstract class OreGlobNode {

    @Nullable
    private OreGlobNode next;
    private boolean negated;

    @Nullable
    private MatchDescription descriptionCache;

    // package-private constructor to prevent inheritance from outside
    OreGlobNode() {}

    @Nullable
    public final OreGlobNode getNext() {
        return next;
    }

    public final boolean hasNext() {
        return next != null;
    }

    final void setNext(@Nullable OreGlobNode next) {
        if (this.next != next) {
            this.next = next;
            clearMatchDescriptionCache();
        }
    }

    public final boolean isNegated() {
        return negated;
    }

    final void setNegated(boolean negated) {
        if (this.negated != negated) {
            this.negated = negated;
            clearMatchDescriptionCache();
        }
    }

    /**
     * Visit this node.
     *
     * @param visitor Visitor for this node
     */
    public abstract void visit(NodeVisitor visitor);

    /**
     * Whether this node shares same structure and content with given node.
     * The check includes types, type specific states, negation flag and
     * the next node's structural equality.
     * <p>
     * Note that this check does not account for logical equivalency outside
     * structural equality.
     *
     * @param node The node to check
     * @return Whether this node shares same structure and content with given node
     */
    public boolean isStructurallyEqualTo(@NotNull OreGlobNode node) {
        if (this == node) return true;
        if (this.isNegated() != node.isNegated()) return false;
        return isPropertyEqualTo(node) && isStructurallyEqualTo(this.getNext(), node.getNext());
    }

    public final MatchDescription getMatchDescription() {
        if (this.descriptionCache == null) {
            MatchDescription t = getIndividualNodeMatchDescription();
            if (t != MatchDescription.NOTHING && this.getNext() != null)
                t = t.append(this.getNext().getMatchDescription());
            return this.descriptionCache = t;
        }
        return this.descriptionCache;
    }

    public final boolean is(MatchDescription description) {
        return getMatchDescription() == description;
    }

    /**
     * Whether this node has same type and property with given node. The check includes types and
     * type specific states. Other properties such as negation and next node are ignored.
     *
     * @param node The node to check
     * @return Whether this node has same type and property with given node
     */
    public abstract boolean isPropertyEqualTo(@NotNull OreGlobNode node);

    /**
     * @return Match type of this specific node
     */
    protected abstract MatchDescription getIndividualNodeMatchDescription();

    final void clearMatchDescriptionCache() {
        this.descriptionCache = null;
    }

    public static boolean isStructurallyEqualTo(@Nullable OreGlobNode node1, @Nullable OreGlobNode node2) {
        if (node1 == node2) return true;
        if (node1 == null) return node2.is(MatchDescription.EMPTY);
        if (node2 == null) return node1.is(MatchDescription.EMPTY);
        return node1.isStructurallyEqualTo(node2);
    }
}
