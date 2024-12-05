package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;

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

    public static void flood(@NotNull IGraphNet net,
                             @NotNull ObjIntConsumer<NetNode> flowReporterNode,
                             @NotNull ObjIntConsumer<NetEdge> flowReporterEdge,
                             @NotNull ToIntFunction<NetEdge> capacityFunction,
                             @NotNull ToIntFunction<NetNode> supplyFunction,
                             @Nullable ToBooleanFunction<NetNode> lossyNodes) {
        if (!net.getGraph().isDirected()) {
            throw new IllegalArgumentException("Cannot perform flood traverse logic on undirected graph!");
        }
        EvaluationResult result = new FDTraverse(net.getGraph(), capacityFunction, supplyFunction, lossyNodes).evaluate();
        if (result.isEmpty()) return;
        result.getFlowMap().forEach(flowReporterEdge::accept);
        result.getSupplyMap().forEach(flowReporterNode::accept);
    }

    protected FDTraverse(Graph<GraphVertex, GraphEdge> graph, ToIntFunction<NetEdge> capacityFunction,
                         ToIntFunction<NetNode> supplyFunction, @Nullable ToBooleanFunction<NetNode> lossyNodes) {
        this.graph = graph;
        this.capacityFunction = capacityFunction;
        this.supplyFunction = supplyFunction;
        this.lossyNodes = lossyNodes != null ? lossyNodes : n -> false;
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
