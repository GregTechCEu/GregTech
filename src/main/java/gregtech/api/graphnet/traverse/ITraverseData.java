package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.path.INetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import gregtech.api.graphnet.traverse.util.ReversibleLossOperator;

import org.jetbrains.annotations.Nullable;

public interface ITraverseData<N extends NetNode, P extends INetPath<N, ?>> {

    IGraphNet getGraphNet();

    IPredicateTestObject getTestObject();

    @Nullable SimulatorKey getSimulatorKey();

    long getQueryTick();

    /**
     * Called before walking the next path. Should reset per-path logics to prepare.
     * @param path the next path
     * @return whether the path should be skipped
     */
    boolean prepareForPathWalk(P path);

    /**
     * Reports that the traverse is traversing to a node, for additional logic to be run.
     *
     * @param node the node being traversed
     * @param flowReachingNode the flow that has reached this node.
     * @return the loss operator for the node.
     */
    ReversibleLossOperator traverseToNode(N node, long flowReachingNode);

    /**
     * Reports that the traverse has finished a path walk, for finalization.
     *
     * @param destination the active node the path terminated at.
     * @param flowReachingDestination the flow that reached the destination
     * @return the amount of flow that should be consumed, before walking the next path.
     */
    long finalizeAtDestination(N destination, long flowReachingDestination);

    /**
     * Allows for reporting a smaller capacity along an edge than it actually has. Do not report a larger capacity
     * than the actual edge or things will break.
     * @param edge the edge to get capacity for.
     * @return a non-negative capacity that is less than or equal to the true capacity of the edge.
     */
    default long getFlowLimit(AbstractNetFlowEdge edge) {
        return edge.getFlowLimit(this.getTestObject(), this.getGraphNet(), this.getQueryTick(), this.getSimulatorKey());
    }
}
