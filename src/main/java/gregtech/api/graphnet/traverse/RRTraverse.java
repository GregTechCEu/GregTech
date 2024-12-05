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
import org.jgrapht.alg.util.Pair;

import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class RRTraverse extends AbstractMinCostTraverse {

    protected final Graph<GraphVertex, GraphEdge> graph;
    protected final ToIntFunction<NetEdge> capacityFunction;
    protected final ToIntFunction<NetNode> supplyFunction;
    protected final ToBooleanFunction<NetNode> lossyNodes;

    protected Pair<NetNode, NetNode> nextPair;

    public static void roundRobin(@NotNull IGraphNet net,
                                  Supplier<@Nullable Pair<NetNode, NetNode>> nextNodePairSupplier,
                                  @NotNull ObjIntConsumer<NetNode> flowReporterNode,
                                  @NotNull ObjIntConsumer<NetEdge> flowReporterEdge,
                                  @NotNull ToIntFunction<NetEdge> capacityFunction,
                                  @NotNull ToIntFunction<NetNode> supplyFunction,
                                  @Nullable ToBooleanFunction<NetNode> lossyNodes) {
        if (!net.getGraph().isDirected()) {
            throw new IllegalArgumentException("Cannot perform RR traverse logic on undirected graph!");
        }
        RRTraverse traverse = new RRTraverse(net.getGraph(), capacityFunction, supplyFunction, lossyNodes);
        while ((traverse.nextPair = nextNodePairSupplier.get()) != null) {
            EvaluationResult result = traverse.evaluate();
            if (result.isEmpty()) continue;
            result.getFlowMap().forEach(flowReporterEdge::accept);
            result.getSupplyMap().forEach(flowReporterNode::accept);
        }
    }

    protected RRTraverse(Graph<GraphVertex, GraphEdge> graph, ToIntFunction<NetEdge> capacityFunction,
                         ToIntFunction<NetNode> supplyFunction, @Nullable ToBooleanFunction<NetNode> lossyNodes) {
        this.graph = graph;
        this.capacityFunction = capacityFunction;
        this.supplyFunction = supplyFunction;
        this.lossyNodes = lossyNodes != null ? lossyNodes : n -> false;
    }

    @Override
    protected int getSupply(NetNode node) {
        if (lossyNodes.applyAsBool(node)) return Short.MIN_VALUE;
        if (nextPair == null || nextPair.hasElement(node)) return 0;
        return supplyFunction.applyAsInt(node);
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
