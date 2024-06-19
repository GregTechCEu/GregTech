package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ShortestPathsAlgorithm<PT extends Enum<PT> & IPipeType<NDT>,
        NDT extends INodeData<NDT>, E extends NetEdge> extends CHManyToManyShortestPaths<NetNode<PT, NDT, E>, E>
                                         implements INetAlgorithm<PT, NDT, E> {

    public ShortestPathsAlgorithm(Graph<NetNode<PT, NDT, E>, E> graph) {
        super(graph);
    }

    @Override
    public List<NetPath<PT, NDT, E>> getPathsList(NetNode<PT, NDT, E> source) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        List<NetPath<PT, NDT, E>> paths = new ObjectArrayList<>();
        paths.add(new NetPath<>(source));
        // if the source has no group, it has no paths other than the path to itself.
        if (source.getGroupUnsafe() == null) return paths;
        ManyToManyShortestPaths<NetNode<PT, NDT, E>, E> manyToManyPaths = getManyToManyPaths(
                Collections.singleton(source), source.getGroupSafe().getNodes());
        for (NetNode<PT, NDT, E> v : source.getGroupSafe().getNodes()) {
            if (v == source) continue;
            GraphPath<NetNode<PT, NDT, E>, E> path = manyToManyPaths.getPath(source, v);
            if (path != null) {
                paths.add(new NetPath<>(path));
            }
        }
        paths.sort(Comparator.comparingDouble(NetPath::getWeight));
        return paths;
    }
}
