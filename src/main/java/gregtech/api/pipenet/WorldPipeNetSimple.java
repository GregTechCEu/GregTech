package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

public abstract class WorldPipeNetSimple<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>>
                                        extends WorldPipeNetBase<NodeDataType, PipeType, NetEdge> {

    /**
     * Standard pipenet representation. Provides the base form of the pipenet abstraction.
     *
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections
     *                     or unidirectional covers.
     * @param isSinglePath Determines whether this net allows only one source and one destination per group.
     *                     Allows for optimizations in path lookup and cache invalidation.
     */
    public WorldPipeNetSimple(String name, boolean isDirected, boolean isSinglePath) {
        super(name, isDirected, isSinglePath,
                isDirected ? new SimpleDirectedWeightedGraph<>(NetEdge.class) :
                        new SimpleWeightedGraph<>(NetEdge.class));
    }
}
