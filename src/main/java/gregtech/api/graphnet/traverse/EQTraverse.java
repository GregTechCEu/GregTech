package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.group.NetGroup;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.util.GTUtility;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.Comparator;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class EQTraverse extends AbstractMinCostTraverse {

    protected final Graph<GraphVertex, GraphEdge> graph;
    protected final ToIntFunction<NetEdge> capacityFunction;
    protected final ToIntFunction<NetNode> supplyFunction;
    protected @Nullable Predicate<NetNode> lossyNodes;

    protected final Set<NetNode> suppliers;
    protected final Set<NetNode> consumers;
    protected TestCase testCase;

    /**
     * Perform equal distribution traverse. Equal distribution traverse draws the same amount from all sources,
     * and deposits a separately calculated amount to all sinks. Drawn and deposited amounts will be maximized,
     * and loss will be ignored until the final stage of traverse and reporting.
     *
     * @param group            the net group the traverse takes place in
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
    public static int equalDistribution(@NotNull NetGroup group,
                                        @NotNull ObjIntConsumer<NetNode> flowReporterNode,
                                        @NotNull ObjIntConsumer<NetEdge> flowReporterEdge,
                                        @NotNull ToIntFunction<NetEdge> capacityFunction,
                                        @NotNull ToIntFunction<NetNode> supplyFunction,
                                        @Nullable Predicate<NetNode> lossyNodes,
                                        @Nullable ObjIntConsumer<NetNode> lossReporter) {
        if (!group.getGraphView().getType().isDirected()) {
            throw new IllegalArgumentException("Cannot perform equal distribution traverse logic on undirected graph!");
        }
        int minSupplier = Integer.MAX_VALUE;
        Set<NetNode> suppliers = new ObjectOpenHashSet<>();
        int minConsumer = Integer.MIN_VALUE;
        Set<NetNode> consumers = new ObjectOpenHashSet<>();
        for (GraphVertex v : group.getGraphView().vertexSet()) {
            if (v.getWrapped() != null) {
                int supply = supplyFunction.applyAsInt(v.getWrapped());
                if (supply > 0) {
                    minSupplier = Math.min(minSupplier, supply);
                    suppliers.add(v.getWrapped());
                } else if (supply < 0) {
                    minConsumer = Math.max(minConsumer, supply);
                    consumers.add(v.getWrapped());
                }
            }
        }
        if (suppliers.size() == 0 || consumers.size() == 0) return 0;
        // do some math to determine the working multiples that need to be tested
        // supplier count * test supply must equal consumer count * test consumption for all tests
        // naive brute force search for now
        ObjectArrayList<TestCase> cases = new ObjectArrayList<>();
        for (int s = 1; s <= minSupplier; s++) {
            for (int c = -1; c >= minConsumer; c--) {
                if (suppliers.size() * s == consumers.size() * -c)
                    cases.add(new TestCase(minSupplier, minConsumer));
            }
        }
        // sort the cases from max flow to min flow
        TestCase[] arr = cases.stream().sorted(Comparator.comparingInt(c -> suppliers.size() * c.supply))
                .toArray(TestCase[]::new);
        // execute binary searching on test cases
        EQTraverse traverse = new EQTraverse(group.getGraphView(), capacityFunction, supplyFunction, suppliers,
                consumers);
        int solution = (int) GTUtility.binarySearch(0, arr.length - 1, l -> {
            traverse.testCase = arr[(int) l];
            EvaluationResult result = traverse.evaluate();
            // a success requires flow to have happened,
            // supply map to contain all suppliers and consumers and nothing else,
            // and all supply values to match either the supply or consumption for the test case.
            return !result.isEmpty() &&
                    result.getSupplyMap().size() == traverse.suppliers.size() + traverse.consumers.size() &&
                    result.getSupplyMap().object2IntEntrySet().stream().map(Object2IntMap.Entry::getIntValue)
                            .allMatch(i -> i == traverse.testCase.supply || i == traverse.testCase.consumption);
        }, false);
        // finally, evaluate with loss enabled
        traverse.testCase = arr[solution];
        traverse.lossyNodes = lossyNodes;
        EvaluationResult result = traverse.evaluate();
        if (result.isEmpty()) return 0;
        result.getFlowMap().forEach(flowReporterEdge::accept);
        return FDTraverse.reportSupply(flowReporterNode, lossyNodes, lossReporter, result);
    }

    protected EQTraverse(Graph<GraphVertex, GraphEdge> graph, ToIntFunction<NetEdge> capacityFunction,
                         ToIntFunction<NetNode> supplyFunction, Set<NetNode> suppliers, Set<NetNode> consumers) {
        this.graph = graph;
        this.capacityFunction = capacityFunction;
        this.supplyFunction = supplyFunction;
        this.suppliers = suppliers;
        this.consumers = consumers;
    }

    @Override
    protected int getSupply(NetNode node) {
        if (lossyNodes != null && lossyNodes.test(node)) return Short.MIN_VALUE;
        if (suppliers.contains(node)) return testCase.supply;
        if (consumers.contains(node)) return testCase.consumption;
        return 0;
    }

    @Override
    protected int getCapacity(NetEdge edge) {
        return capacityFunction.applyAsInt(edge);
    }

    @Override
    public Graph<GraphVertex, GraphEdge> getGraph() {
        return graph;
    }

    @Desugar
    protected record TestCase(int supply, int consumption) {}
}
