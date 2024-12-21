package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.group.NetGroup;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class RRTraverse extends FDTraverse {

    protected java.util.function.Predicate<NetNode> nextPredicate;

    /**
     * Perform round robin traverse. Round robin traverse asks repeatedly for a node predicate;
     * each predicate will be evaluated independently using flood traverse, where only nodes that match the predicate
     * or have loss are allowed to have supply/demand.
     *
     * @param group                     the net group the traverse takes place in
     * @param nextNodePredicateSupplier supplier for next predicate. Will be repeatedly queried until it returns null,
     *                                  at which point the traverse will exit.
     * @param flowReporterNode          flow reporter for nodes. Positive values mean draw, negative values mean sink.
     * @param flowReporterEdge          flow reporter for edges. Always positive.
     * @param capacityFunction          capacity function for edges.
     * @param supplyFunction            supply function for nodes. Positive values mean available draw, negative values
     *                                  mean available sink.
     * @param lossyNodes                optional function that marks nodes as lossy. Lossy nodes will eat up to
     *                                  {@link Short#MIN_VALUE} flow and their normal supply will be ignored.
     * @param lossReporter              optional reporter for loss. Always negative. Does nothing if lossy nodes is
     *                                  {@code null}.
     * @return the total draw/sink after evaluation.
     */
    public static int roundRobin(@NotNull NetGroup group,
                                 Supplier<@Nullable Predicate<NetNode>> nextNodePredicateSupplier,
                                 @NotNull ObjIntConsumer<NetNode> flowReporterNode,
                                 @NotNull ObjIntConsumer<NetEdge> flowReporterEdge,
                                 @NotNull ToIntFunction<NetEdge> capacityFunction,
                                 @NotNull ToIntFunction<NetNode> supplyFunction,
                                 @Nullable Predicate<NetNode> lossyNodes,
                                 @Nullable ObjIntConsumer<NetNode> lossReporter) {
        if (!group.getGraphView().getType().isDirected()) {
            throw new IllegalArgumentException("Cannot perform RR traverse logic on undirected graph!");
        }
        RRTraverse traverse = new RRTraverse(group.getGraphView(), capacityFunction, supplyFunction, lossyNodes);

        int flow = 0;
        while ((traverse.nextPredicate = nextNodePredicateSupplier.get()) != null) {
            EvaluationResult result = traverse.evaluate();
            if (result.isEmpty()) continue;
            result.getFlowMap().forEach(flowReporterEdge::accept);
            flow += reportSupply(flowReporterNode, lossyNodes, lossReporter, result);
        }
        return flow;
    }

    protected RRTraverse(Graph<GraphVertex, GraphEdge> graph, ToIntFunction<NetEdge> capacityFunction,
                         ToIntFunction<NetNode> supplyFunction, @Nullable Predicate<NetNode> lossyNodes) {
        super(graph, capacityFunction, supplyFunction, lossyNodes);
    }

    @Override
    protected int getSupply(NetNode node) {
        if (lossyNodes.test(node)) return Short.MIN_VALUE;
        if (nextPredicate == null || nextPredicate.test(node)) return 0;
        return supplyFunction.applyAsInt(node);
    }
}
