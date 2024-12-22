package gregtech.api.graphnet;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.traverse.iter.EdgeDirection;
import gregtech.api.graphnet.traverse.iter.EdgeSelector;
import gregtech.api.graphnet.traverse.iter.NetClosestIterator;

import org.jetbrains.annotations.NotNull;

public final class GraphNetUtility {

    private GraphNetUtility() {}

    public static boolean isOnlyBridge(@NotNull NetEdge bridge) {
        NetNode sourceNode = bridge.getSource();
        NetNode destNode = bridge.getTarget();
        if (sourceNode == null || destNode == null) return false;
        EdgeSelector selector = bridgeFiltered(EdgeDirection.ALL, sourceNode, destNode);
        NetClosestIterator sourceFrontier = new NetClosestIterator(sourceNode, selector);
        NetClosestIterator destFrontier = new NetClosestIterator(destNode, selector);
        // since we check all edges, if either frontier exhausts we know that they cannot coincide.
        while (sourceFrontier.hasNext() && destFrontier.hasNext()) {
            NetNode next = sourceFrontier.next();
            // the dest frontier has seen the next node in the source frontier, we are not the only bridge.
            if (destFrontier.hasSeen(next)) return false;
            next = destFrontier.next();
            // the source frontier has seen the next node in the dest frontier, we are not the only bridge.
            if (sourceFrontier.hasSeen(next)) return false;
        }
        return true;
    }

    public static EdgeSelector bridgeFiltered(@NotNull EdgeSelector basis,
                                              @NotNull NetNode sourceNode, @NotNull NetNode destNode) {
        GraphEdge e1 = GraphEdge.unwrap(sourceNode.getNet().getEdge(sourceNode, destNode));
        GraphEdge e2 = GraphEdge.unwrap(sourceNode.getNet().getEdge(destNode, sourceNode));
        GraphVertex v1 = GraphVertex.unwrap(sourceNode);
        GraphVertex v2 = GraphVertex.unwrap(destNode);
        return null;
    }
}
