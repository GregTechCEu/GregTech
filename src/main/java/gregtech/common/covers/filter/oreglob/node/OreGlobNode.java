package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Syntax tree representation of ore expression.
 */
public abstract class OreGlobNode {

    @Nullable
    private OreGlobNode next;
    private boolean inverted;

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

    public final boolean isInverted() {
        return inverted;
    }

    final void setInverted(boolean inverted) {
        if (this.inverted != inverted) {
            this.inverted = inverted;
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
    public final OreGlobNode visit(NodeVisitor visitor) {
        switch (this.getMatchDescription()) {
            case EVERYTHING:
                visitor.everything();
                break;
            case IMPOSSIBLE:
                visitor.impossible();
                break;
            case SOMETHING:
                visitor.something();
                break;
            case NOTHING:
                visitor.nothing();
                break;
            default:
                visitInternal(visitor);
        }
        return this.getNext();
    }

    protected abstract void visitInternal(NodeVisitor visitor);

    /**
     * Whether this node shares same structure and content with given node.
     * The check includes types, type specific states, 'inverted' flag and
     * the next node's structural equality.<p>
     * Note that this check does not account for logical equivalency outside
     * structural equality.
     *
     * @param node The node to check
     * @return Whether this node shares same structure and content with given node
     */
    public boolean isStructurallyEqualTo(@Nonnull OreGlobNode node) {
        if (this == node) return true;
        if (this.isInverted() != node.isInverted()) return false;
        return isPropertyEqualTo(node) && isStructurallyEqualTo(this.getNext(), node.getNext());
    }

    public final MatchDescription getMatchDescription() {
        if (this.descriptionCache == null) {
            MatchDescription t = getSelfMatchDescription();
            if (t != MatchDescription.IMPOSSIBLE && this.getNext() != null)
                t = t.append(this.getNext().getMatchDescription());
            return this.descriptionCache = t;
        }
        return this.descriptionCache;
    }

    public final MatchDescription getSelfMatchDescription() {
        if (this.selfDescriptionCache == null) {
            MatchDescription t = getIndividualNodeMatchDescription();
            if (this.isInverted() && !(this instanceof ErrorNode)) t = t.inverse();
            return this.selfDescriptionCache = t;
        }
        return this.selfDescriptionCache;
    }

    public final boolean isEverything() {
        return getMatchDescription() == MatchDescription.EVERYTHING;
    }

    public final boolean isImpossibleToMatch() {
        return getMatchDescription() == MatchDescription.IMPOSSIBLE;
    }

    public final boolean isSomething() {
        return getMatchDescription() == MatchDescription.SOMETHING;
    }

    public final boolean isNothing() {
        return getMatchDescription() == MatchDescription.NOTHING;
    }

    /**
     * Whether this node has same type and property with given node. The check includes types and
     * type specific states. Other properties such as inversion and next node are ignored.
     *
     * @param node The node to check
     * @return Whether this node has same type and property with given node
     */
    public abstract boolean isPropertyEqualTo(@Nonnull OreGlobNode node);

    /**
     * @return Match type of this specific node, without considering inverted flag.
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

