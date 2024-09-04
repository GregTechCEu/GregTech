package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.iter.IteratorFactory;
import gregtech.api.graphnet.alg.iter.SimpleIteratorFactories;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.INetPath;

import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShortestPathsAlgorithm extends CHManyToManyShortestPaths<GraphVertex, GraphEdge>
                                          implements INetAlgorithm {

    private final boolean recomputeEveryCall;

    public ShortestPathsAlgorithm(IGraphNet net, boolean recomputeEveryCall) {
        super(net.getGraph());
        this.recomputeEveryCall = recomputeEveryCall;
    }

    @Override
    public <Path extends INetPath<?, ?>> IteratorFactory<Path> getPathsIteratorFactory(GraphVertex source,
                                                                                       NetPathMapper<Path> remapper) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        // if the source has no group, it has no paths other than the path to itself.
        if (source.wrapped.getGroupUnsafe() == null) {
            Path path = remapper.map(source);
            return SimpleIteratorFactories.fromSingleton(path);
        }

        Set<GraphVertex> searchSpace = source.wrapped.getGroupSafe().getNodes().stream().filter(NetNode::isActive)
                .map(n -> n.wrapper).filter(node -> !source.equals(node) && graph.containsVertex(node)).collect(Collectors.toSet());
        Set<GraphVertex> singleton = Collections.singleton(source);
        if (recomputeEveryCall) {
            return (graph1, testObject, simulator, queryTick) -> {
                graph1.prepareForAlgorithmRun(testObject, simulator, queryTick);
                ManyToManyShortestPaths<GraphVertex, GraphEdge> manyToManyPaths = getManyToManyPaths(singleton,
                        searchSpace);
                return searchSpace.stream().map(node -> manyToManyPaths.getPath(source, node))
                        .map(remapper::map).sorted(Comparator.comparingDouble(INetPath::getWeight)).iterator();
            };
        } else {
            ManyToManyShortestPaths<GraphVertex, GraphEdge> manyToManyPaths = getManyToManyPaths(singleton,
                    searchSpace);
            return SimpleIteratorFactories.fromIterable(searchSpace.stream()
                    .map(node -> manyToManyPaths.getPath(source, node))
                    .map(remapper::map).sorted(Comparator.comparingDouble(INetPath::getWeight))
                    .collect(Collectors.toList()));
        }
    }
}
