package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.iter.SimpleCacheableIterator;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.INetPath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShortestPathsAlgorithm extends CHManyToManyShortestPaths<GraphVertex, GraphEdge>
                                          implements INetAlgorithm {

    public ShortestPathsAlgorithm(IGraphNet net) {
        super(net.getGraph());
    }

    @Override
    public <Path extends INetPath<?, ?>> Iterator<Path> getPathsIterator(GraphVertex source,
                                                                         NetPathMapper<Path> remapper) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        // if the source has no group, it has no paths other than the path to itself.
        if (source.wrapped.getGroupUnsafe() == null) return Collections.singletonList(remapper.map(source)).iterator();

        Set<GraphVertex> searchSpace = source.wrapped.getGroupSafe().getNodes().stream()
                .filter(NetNode::isActive).map(n -> n.wrapper).collect(Collectors.toSet());
        ManyToManyShortestPaths<GraphVertex, GraphEdge> manyToManyPaths = getManyToManyPaths(
                Collections.singleton(source), searchSpace);
        return new SimpleCacheableIterator<>(searchSpace.stream().map(node -> manyToManyPaths.getPath(source, node))
                .map(remapper::map).sorted(Comparator.comparingDouble(INetPath::getWeight))
                .collect(Collectors.toCollection(ObjectArrayList::new)));
    }
}
