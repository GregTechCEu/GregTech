package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.WorldPipeNetBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShortestPathsAlgorithm<PT extends Enum<PT> & IPipeType<NDT>,
        NDT extends INodeData<NDT>, E extends NetEdge> extends CHManyToManyShortestPaths<NetNode<PT, NDT, E>, E>
                                         implements INetAlgorithm<PT, NDT, E> {

    public ShortestPathsAlgorithm(WorldPipeNetBase<NDT, PT, E> pipenet) {
        super(pipenet.getGraph());
    }

    @Override
    public List<NetPath<PT, NDT, E>> getPathsList(NetNode<PT, NDT, E> source) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        // if the source has no group, it has no paths other than the path to itself.
        if (source.getGroupUnsafe() == null) return Collections.singletonList(new NetPath<>(source));

        Set<NetNode<PT, NDT, E>> searchSpace = source.getGroupSafe().getNodes().stream()
                .filter(NetNode::isActive).collect(Collectors.toSet());
        ManyToManyShortestPaths<NetNode<PT, NDT, E>, E> manyToManyPaths = getManyToManyPaths(
                Collections.singleton(source), searchSpace);
        return searchSpace.stream().map(node -> manyToManyPaths.getPath(source, node)).map(NetPath::new)
                .sorted(Comparator.comparingDouble(NetPath::getWeight)).collect(Collectors.toList());
    }
}
