package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.alg.NetPathMapper;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.path.AbstractNetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BasicWorldPipeNetPath extends AbstractNetPath<WorldPipeNetNode, NetEdge> {

    public static final NetPathMapper<BasicWorldPipeNetPath> MAPPER = new NetPathMapper<>(BasicWorldPipeNetPath::new,
            BasicWorldPipeNetPath::new, BasicWorldPipeNetPath::new);

    public BasicWorldPipeNetPath(GraphVertex vertex) {
        this(Collections.singletonList(vertex), Collections.emptyList(),
                vertex.wrapped.getData().getLogicEntryDefaultable(WeightFactorLogic.INSTANCE).getValue());
    }

    public BasicWorldPipeNetPath(List<GraphVertex> vertices, List<GraphEdge> edges, double weight) {
        super(vertices.stream().map(v -> (WorldPipeNetNode) v.wrapped).collect(Collectors.toList()),
                edges.stream().map(e -> e.wrapped).collect(Collectors.toList()), weight);
    }

    public BasicWorldPipeNetPath(GraphPath<GraphVertex, GraphEdge> path) {
        this(path.getVertexList(), path.getEdgeList(), path.getWeight());
    }

    public interface Provider {

        Iterator<BasicWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                 @Nullable SimulatorKey simulator, long queryTick);
    }
}