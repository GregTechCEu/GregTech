package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.ToBooleanFunction;

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
import java.util.function.ToIntFunction;

public class EQTraverse extends AbstractMinCostTraverse {

    protected final Graph<GraphVertex, GraphEdge> graph;
    protected final ToIntFunction<NetEdge> capacityFunction;
    protected final ToIntFunction<NetNode> supplyFunction;
    protected @Nullable ToBooleanFunction<NetNode> lossyNodes;

    protected final Set<NetNode> suppliers;
    protected final Set<NetNode> consumers;
    protected TestCase testCase;

    public static void equalDistribution(@NotNull IGraphNet net,
                                         @NotNull ObjIntConsumer<NetNode> flowReporterNode,
                                         @NotNull ObjIntConsumer<NetEdge> flowReporterEdge,
                                         @NotNull ToIntFunction<NetEdge> capacityFunction,
                                         @NotNull ToIntFunction<NetNode> supplyFunction,
                                         @Nullable ToBooleanFunction<NetNode> lossyNodes) {
        if (!net.getGraph().isDirected()) {
            throw new IllegalArgumentException("Cannot perform equal distribution traverse logic on undirected graph!");
        }
        int minSupplier = Integer.MAX_VALUE;
        Set<NetNode> suppliers = new ObjectOpenHashSet<>();
        int minConsumer = Integer.MIN_VALUE;
        Set<NetNode> consumers = new ObjectOpenHashSet<>();
        for (GraphVertex v : net.getGraph().vertexSet()) {
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
        if (suppliers.size() == 0 || consumers.size() == 0) return;
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
        EQTraverse traverse = new EQTraverse(net.getGraph(), capacityFunction, supplyFunction, suppliers, consumers);
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
        if (result.isEmpty()) return;
        result.getFlowMap().forEach(flowReporterEdge::accept);
        result.getSupplyMap().forEach(flowReporterNode::accept);
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
        if (lossyNodes != null && lossyNodes.applyAsBool(node)) return Short.MIN_VALUE;
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
