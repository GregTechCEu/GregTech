package gregtech.api.graphnet.group;

import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.edge.NetEdge;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class GroupData {

    private static final Pair<GroupData, GroupData> EMPTY = ImmutablePair.of(null, null);

    private NetGroup group;

    @MustBeInvokedByOverriders
    public void withGroup(@NotNull NetGroup group) {
        this.group = group;
    }

    @Nullable
    public NetGroup getGroup() {
        return group;
    }

    /**
     * Used to determine if merging two groups is allowed. Will be called in both directions. If the merge is allowed,
     * {@link #mergeAcross(GroupData, NetEdge)} will be called later after the graph is modified.
     * 
     * @param other the group data of the other group
     * @return whether they can be merged. Completely blocks edge creation if false.
     */
    protected boolean mergeAllowed(@Nullable GroupData other) {
        return other == null || other.getClass() == this.getClass();
    }

    /**
     * Used to determine if merging two groups is allowed. Will test both directions. If the merge is allowed,
     * {@link #mergeAcross(GroupData, NetEdge)} will be called later after the graph is modified.
     * 
     * @param source the first group data
     * @param target the second group data
     * @return which datas authorized the merge. Completely blocks edge creation if none.
     */
    @NotNull
    public static MergeDirection mergeAllowed(@Nullable GroupData source, @Nullable GroupData target) {
        if (source != null && source.mergeAllowed(target)) return MergeDirection.SOURCE;
        if (target != null && target.mergeAllowed(source)) return MergeDirection.TARGET;
        return MergeDirection.NONE;
    }

    /**
     * Called when a new edge bridges the interior of a net group rather than connecting two separate net groups.
     * 
     * @param edge the bridging edge
     */
    public void notifyOfBridgingEdge(@NotNull NetEdge edge) {}

    /**
     * Called when an edge belonging to a group is removed, before the graph is modified. If this splits the group,
     * {@link #splitAcross(Set, Set)} will be called later after the graph is modified.
     * 
     * @param edge the edge removed.
     */
    public void notifyOfRemovedEdge(@NotNull NetEdge edge) {}

    /**
     * Merge data across an edge. Accompanies the process of merging groups.
     * 
     * @param other the group data to merge with.
     * @param edge  the edge merged across
     * @return the result of the merge.
     */
    @Nullable
    protected GroupData mergeAcross(@Nullable GroupData other, @NotNull NetEdge edge) {
        if (other != null) return null;
        else return this;
    }

    /**
     * Split data across an edge. Accompanies the process of splitting groups.
     * 
     * @param sourceNodes the first set of nodes.
     * @param targetNodes the second set of nodes.
     * @return a pair, where the first value is the group data associated with the first set of nodes, and the second
     *         value is the group data associated with the second set of nodes.
     */
    @NotNull
    public Pair<GroupData, GroupData> splitAcross(@NotNull Set<NetNode> sourceNodes,
                                                  @NotNull Set<NetNode> targetNodes) {
        return EMPTY;
    }
}
