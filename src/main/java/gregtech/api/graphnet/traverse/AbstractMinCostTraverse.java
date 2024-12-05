package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.util.GTUtility;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;

import java.util.function.Function;

public abstract class AbstractMinCostTraverse implements MinimumCostFlowProblem<GraphVertex, GraphEdge> {

    protected static final CapacityScalingMinimumCostFlow<GraphVertex, GraphEdge> MINCOST = new CapacityScalingMinimumCostFlow<>();

    protected static final GraphVertex CORRECTOR = new GraphVertex();

    protected int correction = 0;

    public @NotNull EvaluationResult evaluate() {
        try {
            correction = 0;
            getGraph().addVertex(CORRECTOR);
            int count = 0;
            for (GraphVertex v : getGraph().vertexSet()) {
                if (v.getWrapped() != null) {
                    int supply = getSupply(v.getWrapped());
                    if (supply != 0) {
                        correction -= supply;
                        GraphEdge e = new GraphEdge();
                        if (supply < 0) {
                            getGraph().addEdge(CORRECTOR, v, e);
                        } else {
                            getGraph().addEdge(v, CORRECTOR, e);
                        }
                        getGraph().setEdgeWeight(e, CapacityScalingMinimumCostFlow.COST_INF - 1);
                        count++;
                    }
                }
            }
            MinimumCostFlowAlgorithm.MinimumCostFlow<GraphEdge> flow = MINCOST.getMinimumCostFlow(this);
            EvaluationResult result = new EvaluationResult(count, flow.getFlowMap().size());
            for (var entry : flow.getFlowMap().entrySet()) {
                NetEdge e = entry.getKey().getWrapped();
                if (e == null) {
                    GraphVertex v = entry.getKey().getOppositeVertex(CORRECTOR);
                    if (v != null && v.getWrapped() != null) {
                        // flow through the corrector is always unsatisfied supply/demand
                        int sat = GTUtility.moveACloserTo0ByB(getSupply(v.getWrapped()), entry.getValue().intValue());
                        result.reportSupply(v.getWrapped(), sat);
                    }
                } else if (entry.getValue().intValue() != 0) {
                    result.reportFlow(e, entry.getValue().intValue());
                }
            }
            getGraph().removeVertex(CORRECTOR);
            return result;
        } catch (Exception ignored) {
            return EvaluationResult.EMPTY;
        }
    }

    @Override
    public Function<GraphEdge, Integer> getArcCapacityLowerBounds() {
        return e -> 0;
    }

    @Override
    public Function<GraphEdge, Integer> getArcCapacityUpperBounds() {
        return e -> e.getWrapped() == null ? CapacityScalingMinimumCostFlow.CAP_INF : getCapacity(e.getWrapped());
    }

    @Override
    public Function<GraphVertex, Integer> getNodeSupply() {
        return v -> v.getWrapped() != null ? getSupply(v.getWrapped()) : v == CORRECTOR ? correction : 0;
    }

    protected abstract int getSupply(NetNode node);

    protected abstract int getCapacity(NetEdge edge);

    public static class EvaluationResult {

        public static final EvaluationResult EMPTY = new EvaluationResult(0, 0);

        protected final Object2IntOpenHashMap<NetEdge> flowMap;
        protected final Object2IntOpenHashMap<NetNode> supplyMap;

        public EvaluationResult(int sizeSupply, int sizeFlow) {
            supplyMap = new Object2IntOpenHashMap<>(sizeSupply);
            flowMap = new Object2IntOpenHashMap<>(sizeFlow);
        }

        public Object2IntOpenHashMap<NetEdge> getFlowMap() {
            return flowMap;
        }

        public Object2IntOpenHashMap<NetNode> getSupplyMap() {
            return supplyMap;
        }

        public void reportFlow(NetEdge edge, int flow) {
            flowMap.put(edge, flow);
        }

        public void reportSupply(NetNode node, int supply) {
            supplyMap.put(node, supply);
        }

        public boolean isEmpty() {
            // if there were no flows, there should be no supply
            return flowMap.isEmpty();
        }
    }
}
