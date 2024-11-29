package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;

import java.util.Collections;
import java.util.function.Function;

public abstract class AbstractMinCostTraverse implements MinimumCostFlowProblem<GraphVertex, GraphEdge> {

    protected static final CapacityScalingMinimumCostFlow<GraphVertex, GraphEdge> MINCOST = new CapacityScalingMinimumCostFlow<>();

    protected static final GraphVertex CORRECTOR = new GraphVertex();

    public Object2DoubleMap<NetEdge> evaluate() {
        try {
            // setup:
            MINCOST.getMinimumCostFlow(this)
        } catch (Exception ignored) {
            return Object2DoubleMaps.emptyMap();
        }
    }

    @Override
    public Graph<GraphVertex, GraphEdge> getGraph() {
        return null;
    }

    @Override
    public Function<GraphVertex, Integer> getNodeSupply() {
        return null;
    }

    @Override
    public Function<GraphEdge, Integer> getArcCapacityLowerBounds() {
        return e -> 0;
    }

    @Override
    public Function<GraphEdge, Integer> getArcCapacityUpperBounds() {
        return null;
    }
}
