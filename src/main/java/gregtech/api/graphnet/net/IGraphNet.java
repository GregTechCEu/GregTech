package gregtech.api.graphnet.net;

import gregtech.api.graphnet.GraphClassType;
import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.WeightFactorLogic;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public interface IGraphNet {

    /**
     * Adds a node to the graphnet.
     * 
     * @param node The node to add.
     */
    void addNode(@NotNull NetNode node);

    /**
     * Gets the net node with the given equivalency data, if one exists.
     * 
     * @param equivalencyData the equivalency data to match.
     * @return the matching net node, if one exists.
     */
    @Nullable
    NetNode getNode(@NotNull Object equivalencyData);

    /**
     * Removes a node from the graphnet.
     * 
     * @param node The node to remove.
     */
    void removeNode(@NotNull NetNode node);

    /**
     * Links two nodes by an edge.
     * 
     * @param source   Source node.
     * @param target   Target node.
     * @param bothWays If the graph is directional, passing in true will create both the forwards and backwards edge.
     * @return the created edge, if it was created. Returns null if bothWays is set to true.
     */
    @Nullable
    @Contract("_, _, false -> _; _, _, true -> null")
    NetEdge addEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays);

    /**
     * Returns the edge linking two nodes together, if one exists.
     * 
     * @param source Source node.
     * @param target Target node.
     * @return the linking edge, if one exists.
     */
    @Nullable
    NetEdge getEdge(@NotNull NetNode source, @NotNull NetNode target);

    /**
     * Removes the edge linking two nodes together, if one exists.
     * 
     * @param source   Source node.
     * @param target   Target node.
     * @param bothWays If the graph is directional, passing in true will remove both the forwards and backwards edge.
     */
    void removeEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays);

    /**
     * Gets the {@link INetGraph} backing this graphnet. This should NEVER be modified directly, but can be queried.
     * 
     * @return the backing net graph
     */
    default @UnmodifiableView INetGraph getGraph() {
        return getBacker().getGraph();
    }

    /**
     * Gets the {@link GraphNetBacker} backing this graphnet. This should NEVER be used except inside the graphnet impl.
     * 
     * @return the backing graphnet backer
     */
    @ApiStatus.Internal
    GraphNetBacker getBacker();

    /**
     * Get a blank group data for this graph. <br>
     * Make sure to override this if your NetGroups use data.
     *
     * @return The correct data variant.
     */
    @Nullable
    default GroupData getBlankGroupData() {
        return null;
    }

    /**
     * Get a default node data for this graph. Generally used for immediate nbt deserialization.
     * 
     * @return A default node data object.
     */
    @NotNull
    @Contract("->new")
    default NetLogicData getDefaultNodeData() {
        return new NetLogicData().setLogicEntry(WeightFactorLogic.TYPE.getWith(1));
    }

    /**
     * Returns whether a node exists in this graph.
     * 
     * @param node the node in question.
     * @return whether the node exists.
     */
    default boolean containsNode(NetNode node) {
        return getGraph().containsVertex(node.wrapper);
    }

    /**
     * Used in {@link MultiNodeHelper} to determine if a node can be traversed, based on the nets that have been
     * recently traversed in the {@link MultiNodeHelper}.
     * 
     * @param net a recently traversed net
     * @return if node traversal should be blocked.
     */
    default boolean clashesWith(IGraphNet net) {
        return false;
    }

    /**
     * @return a new node with no data, to be either nbt deserialized or initialized in some other way.
     */
    @NotNull
    GraphClassType<? extends NetNode> getDefaultNodeType();

    /**
     * @return a new edge with no data, to be either nbt deserialized or initialized in some other way.
     */
    @NotNull
    default GraphClassType<? extends NetEdge> getDefaultEdgeType() {
        return NetEdge.TYPE;
    }

    /**
     * Should only be used by the internal {@link GraphNetBacker} backing this graphnet.
     */
    @ApiStatus.Internal
    void markDirty();
}
