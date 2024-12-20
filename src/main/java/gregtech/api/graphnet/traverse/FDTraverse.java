package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.util.function.ToBooleanFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

public class FDTraverse extends AbstractMinCostTraverse {

    protected final Graph<GraphVertex, GraphEdge> graph;
    protected final ToIntFunction<NetEdge> capacityFunction;
    protected final ToIntFunction<NetNode> supplyFunction;
    protected final ToBooleanFunction<NetNode> lossyNodes;

    /**
     * Perform flood traverse. Flood traverse takes the lowest cost (weight) paths while maximizing flow.
     *
     * @param net              the net
     * @param flowReporterNode flow reporter for nodes. Positive values mean draw, negative values mean sink.
     * @param flowReporterEdge flow reporter for edges. Always positive.
     * @param capacityFunction capacity function for edges.
     * @param supplyFunction   supply function for nodes. Positive values mean available draw, negative values mean
     *                         available sink.
     * @param lossyNodes       optional function that marks nodes as lossy. Lossy nodes will eat up to
     *                         {@link Short#MIN_VALUE} flow and their normal supply will be ignored.
     * @param lossReporter     optional reporter for loss. Always negative. Does nothing if lossy nodes is {@code null}.
     * @return the total draw/sink after evaluation.
     */
    public static int flood(@NotNull IGraphNet net,
                            @NotNull ObjIntConsumer<NetNode> flowReporterNode,
                            @NotNull ObjIntConsumer<NetEdge> flowReporterEdge,
                            @NotNull ToIntFunction<NetEdge> capacityFunction,
                            @NotNull ToIntFunction<NetNode> supplyFunction,
                            @Nullable ToBooleanFunction<NetNode> lossyNodes,
                            @Nullable ObjIntConsumer<NetNode> lossReporter) {
        if (!net.getGraph().isDirected()) {
            throw new IllegalArgumentException("Cannot perform flood traverse logic on undirected graph!");
        }
        EvaluationResult result = new FDTraverse(net.getGraph(), capacityFunction, supplyFunction, lossyNodes)
                .evaluate();
        if (result.isEmpty()) return 0;
        result.getFlowMap().forEach(flowReporterEdge::accept);
        return reportFlow(flowReporterNode, lossyNodes, lossReporter, result);
    }

    protected FDTraverse(Graph<GraphVertex, GraphEdge> graph, ToIntFunction<NetEdge> capacityFunction,
                         ToIntFunction<NetNode> supplyFunction, @Nullable ToBooleanFunction<NetNode> lossyNodes) {
        this.graph = graph;
        this.capacityFunction = capacityFunction;
        this.supplyFunction = supplyFunction;
        this.lossyNodes = lossyNodes != null ? lossyNodes : n -> false;
    }

    static int reportFlow(@NotNull ObjIntConsumer<NetNode> flowReporterNode,
                          @Nullable ToBooleanFunction<NetNode> lossyNodes,
                          @Nullable ObjIntConsumer<NetNode> lossReporter, @NotNull EvaluationResult result) {
        int flow = 0;
        for (var entry : result.getSupplyMap().object2IntEntrySet()) {
            NetNode n = entry.getKey();
            int i = entry.getIntValue();
            if (lossyNodes != null && lossyNodes.applyAsBool(n)) {
                if (lossReporter != null) lossReporter.accept(n, i);
            } else {
                flowReporterNode.accept(n, i);
            }
            if (i > 0) flow += i;
        }
        return flow;
    }

    @Override
    protected int getSupply(NetNode node) {
        if (lossyNodes.applyAsBool(node)) return Short.MIN_VALUE;
        else return supplyFunction.applyAsInt(node);
    }

    @Override
    protected int getCapacity(NetEdge edge) {
        return capacityFunction.applyAsInt(edge);
    }

    @Override
    public Graph<GraphVertex, GraphEdge> getGraph() {
        return graph;
    }
}
