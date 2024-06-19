package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ShortestPathsAlgorithm<PT extends Enum<PT> & IPipeType<NDT>,
        NDT extends INodeData<NDT>> extends CHManyToManyShortestPaths<NodeG<PT, NDT>, NetEdge>
        implements INetAlgorithm<PT, NDT> {

    public ShortestPathsAlgorithm(Graph<NodeG<PT, NDT>, NetEdge> graph) {
        super(graph);
    }

    @Override
    public List<NetPath<PT, NDT>> getPathsList(NodeG<PT, NDT> source) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        List<NetPath<PT, NDT>> paths = new ObjectArrayList<>();
        paths.add(new NetPath<>(source));
        // if the source has no group, it has no paths other than the path to itself.
        if (source.getGroupUnsafe() == null) return paths;
        ManyToManyShortestPaths<NodeG<PT, NDT>, NetEdge> manyToManyPaths = getManyToManyPaths(
                Collections.singleton(source), source.getGroupSafe().getNodes());
        for (NodeG<PT, NDT> v : source.getGroupSafe().getNodes()) {
            if (v == source) continue;
            GraphPath<NodeG<PT, NDT>, NetEdge> path = manyToManyPaths.getPath(source, v);
            if (path != null) {
                paths.add(new NetPath<>(path));
            }
        }
        paths.sort(Comparator.comparingDouble(NetPath::getWeight));
        return paths;
    }
}
