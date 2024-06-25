package gregtech.api.pipenet;

import gregtech.api.pipenet.alg.INetAlgorithm;
import gregtech.api.pipenet.alg.ShortestPathsAlgorithm;
import gregtech.api.pipenet.alg.SinglePathAlgorithm;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.function.Function;

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
     *                     Allows for optimizations in path lookup.
     */
    public WorldPipeNetSimple(String name, boolean isDirected, boolean isSinglePath) {
        this(name, isDirected, isSinglePath ? SinglePathAlgorithm::new : ShortestPathsAlgorithm::new);
    }

    /**
     * Standard pipenet representation. Provides the base form of the pipenet abstraction.
     *
     * @param isDirected       Determines whether this net needs directed graph handling.
     *                         Used to respect filter directions in the item net and fluid net, for example.
     *                         If the graph is not directed, pipes should not support blocked connections
     *                         or unidirectional covers.
     * @param algorithmBuilder custom function to construct a new algorithm when the old one is invalidated.
     */
    public WorldPipeNetSimple(String name, boolean isDirected,
                              Function<WorldPipeNetBase<NodeDataType, PipeType, NetEdge>, INetAlgorithm<PipeType, NodeDataType, NetEdge>> algorithmBuilder) {
        super(name, isDirected, algorithmBuilder, isDirected ? new SimpleDirectedWeightedGraph<>(NetEdge.class) :
                new SimpleWeightedGraph<>(NetEdge.class));
    }
}
