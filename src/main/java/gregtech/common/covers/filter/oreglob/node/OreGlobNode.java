package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Syntax tree representation of ore expression.
 */
public abstract class OreGlobNode {

    @Nullable
    private OreGlobNode next;
    private boolean negated;

    @Nullable
    private MatchDescription descriptionCache;
    @Nullable
    private MatchDescription selfDescriptionCache;

    // package-private constructor to prevent inheritance from outside
    OreGlobNode() {
    }

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
     * @return Next node, if exists
     */
    @Nullable
    public OreGlobNode visit(NodeVisitor visitor) {
        switch (this.getMatchDescription()) {
            case EVERYTHING:
                visitor.everything();
                break;
            case NOTHING:
                visitor.nothing();
                break;
            case NONEMPTY:
                visitor.nonempty();
                break;
            case EMPTY:
                visitor.empty();
                break;
            default:
                visitInternal(visitor);
        }
        return this.getNext();
    }

    protected abstract void visitInternal(NodeVisitor visitor);

    /**
     * Whether this node shares same structure and content with given node.
     * The check includes types, type specific states, negation flag and
     * the next node's structural equality.<p>
     * Note that this check does not account for logical equivalency outside
     * structural equality.
     *
     * @param node The node to check
     * @return Whether this node shares same structure and content with given node
     */
    public boolean isStructurallyEqualTo(@Nonnull OreGlobNode node) {
        if (this == node) return true;
        if (this.isNegated() != node.isNegated()) return false;
        return isPropertyEqualTo(node) && isStructurallyEqualTo(this.getNext(), node.getNext());
    }

    public final MatchDescription getMatchDescription() {
        if (this.descriptionCache == null) {
            MatchDescription t = getSelfMatchDescription();
            if (t != MatchDescription.NOTHING && this.getNext() != null)
                t = t.append(this.getNext().getMatchDescription());
            return this.descriptionCache = t;
        }
        return this.descriptionCache;
    }

    public final MatchDescription getSelfMatchDescription() {
        if (this.selfDescriptionCache == null) {
            MatchDescription t = getIndividualNodeMatchDescription();
            if (this.isNegated() && !(this instanceof ErrorNode)) t = t.complement();
            return this.selfDescriptionCache = t;
        }
        return this.selfDescriptionCache;
    }

    public final boolean isEverything() {
        return getMatchDescription() == MatchDescription.EVERYTHING;
    }

    public final boolean isImpossibleToMatch() {
        return getMatchDescription() == MatchDescription.NOTHING;
    }

    public final boolean isSomething() {
        return getMatchDescription() == MatchDescription.NONEMPTY;
    }

    public final boolean isNothing() {
        return getMatchDescription() == MatchDescription.EMPTY;
    }

    /**
     * Whether this node has same type and property with given node. The check includes types and
     * type specific states. Other properties such as negation and next node are ignored.
     *
     * @param node The node to check
     * @return Whether this node has same type and property with given node
     */
    public abstract boolean isPropertyEqualTo(@Nonnull OreGlobNode node);

    /**
     * @return Match type of this specific node, without considering negation flag.
     */
    protected abstract MatchDescription getIndividualNodeMatchDescription();

    final void clearMatchDescriptionCache() {
        this.descriptionCache = null;
        this.selfDescriptionCache = null;
    }

    public static boolean isStructurallyEqualTo(@Nullable OreGlobNode node1, @Nullable OreGlobNode node2) {
        if (node1 == node2) return true;
        if (node1 == null) return node2.isNothing();
        if (node2 == null) return node1.isNothing();
        return node1.isStructurallyEqualTo(node2);
    }
}

