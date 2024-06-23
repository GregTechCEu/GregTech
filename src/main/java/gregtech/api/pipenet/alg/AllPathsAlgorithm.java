package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.WorldPipeNetBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import org.jgrapht.alg.shortestpath.AllDirectedPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AllPathsAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
        E extends NetEdge> extends AllDirectedPaths<NetNode<PT, NDT, E>, E> implements INetAlgorithm<PT, NDT, E> {

    public AllPathsAlgorithm(WorldPipeNetBase<NDT, PT, E> pipenet) {
        super(pipenet.getGraph());
        if (!pipenet.isDirected()) throw new IllegalArgumentException("Cannot build all paths on an undirected graph!");
    }

    @Override
    public List<NetPath<PT, NDT, E>> getPathsList(NetNode<PT, NDT, E> source) {
        Set<NetNode<PT, NDT, E>> searchSpace = source.getGroupSafe().getNodes().stream()
                .filter(NetNode::isActive).collect(Collectors.toSet());
        return getAllPaths(Collections.singleton(source), searchSpace, true, (int) Short.MAX_VALUE)
                .stream().map(NetPath::new).sorted(Comparator.comparingDouble(NetPath::getWeight))
                .collect(Collectors.toList());
    }
}
