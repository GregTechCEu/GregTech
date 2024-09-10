package gregtech.api.graphnet.path;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.NetPathMapper;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class GenericGraphNetPath extends AbstractNetPath<NetNode, NetEdge> {

    public static final NetPathMapper<GenericGraphNetPath> MAPPER = new NetPathMapper<>(GenericGraphNetPath::new,
            GenericGraphNetPath::new, GenericGraphNetPath::new);

    public GenericGraphNetPath(GraphVertex vertex) {
        this(Collections.singletonList(vertex), Collections.emptyList(),
                vertex.wrapped.getData().getLogicEntryDefaultable(WeightFactorLogic.TYPE).getValue());
    }

    public GenericGraphNetPath(List<GraphVertex> vertices, List<GraphEdge> edges, double weight) {
        super(vertices.stream().map(v -> v.wrapped).collect(Collectors.toList()),
                edges.stream().map(e -> e.wrapped).collect(Collectors.toList()), weight);
    }

    public GenericGraphNetPath(GraphPath<GraphVertex, GraphEdge> path) {
        this(path.getVertexList(), path.getEdgeList(), path.getWeight());
    }

    public interface Provider {

        Iterator<GenericGraphNetPath> getPaths(NetNode node, IPredicateTestObject testObject,
                                               @Nullable SimulatorKey simulator, long queryTick);
    }
}
