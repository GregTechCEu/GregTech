package gregtech.common.covers.filter.oreglob.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Syntax tree representation of ore expression.
 */
public abstract class OreGlobNode {

    @Nullable
    OreGlobNode next;
    boolean inverted;

    @Nullable
    private MatchDescription descriptionCache;
    @Nullable
    private MatchDescription selfDescriptionCache;

    // package-private constructor to prevent inheritance from outside
    OreGlobNode() {
    }

    /**
     * Visit this node.
     *
     * @param visitor Visitor for this node
     * @return Next node, if exists
     */
    @Nullable
    public abstract OreGlobNode visit(NodeVisitor visitor);

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
        if (this.inverted != node.inverted) return false;
        return isPropertyEqualTo(node) && isStructurallyEqualTo(this.next, node.next);
    }

    public final MatchDescription getMatchDescription() {
        if (this.descriptionCache == null) {
            MatchDescription t = getSelfMatchDescription();
            if (t != MatchDescription.IMPOSSIBLE && this.next != null)
                t = t.append(this.next.getMatchDescription());
            return this.descriptionCache = t;
        }
        return this.descriptionCache;
    }

    public final MatchDescription getSelfMatchDescription() {
        if (this.selfDescriptionCache == null) {
            MatchDescription t = getIndividualNodeMatchDescription();
            if (this.inverted && !(this instanceof ErrorNode)) t = t.inverse();
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
    protected MatchDescription getIndividualNodeMatchDescription() {
        return MatchDescription.OTHER_EXCLUDING_NOTHING;
    }

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

