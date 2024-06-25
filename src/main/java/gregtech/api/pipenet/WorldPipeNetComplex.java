package gregtech.api.pipenet;

import gregtech.api.pipenet.alg.INetAlgorithm;
import gregtech.api.pipenet.alg.ShortestPathsAlgorithm;
import gregtech.api.pipenet.alg.SinglePathAlgorithm;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class WorldPipeNetComplex<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        Edge extends NetEdge> extends WorldPipeNetBase<NodeDataType, PipeType, Edge> {

    /**
     * Alternate pipenet representation. Allows for using children of the {@link NetEdge} class as edges.
     * <p>
     * Note - These child edges cannot be allowed to store information, they must only perform runtime behavior.
     *
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections
     *                     or unidirectional covers.
     * @param edgeSupplier The supplier for the custom NetEdge child class.
     * @param isSinglePath Determines whether this net allows only one source and one destination per group.
     *                     Allows for optimizations in path lookup.
     */
    public WorldPipeNetComplex(String name, boolean isDirected, boolean isSinglePath, Supplier<Edge> edgeSupplier) {
        this(name, isDirected, edgeSupplier, isSinglePath ? SinglePathAlgorithm::new : ShortestPathsAlgorithm::new);
    }

    /**
     * Alternate pipenet representation. Allows for using children of the {@link NetEdge} class as edges.
     * <p>
     * Note - These child edges cannot be allowed to store information, they must only perform runtime behavior.
     *
     * @param isDirected       Determines whether this net needs directed graph handling.
     *                         Used to respect filter directions in the item net and fluid net, for example.
     *                         If the graph is not directed, pipes should not support blocked connections
     *                         or unidirectional covers.
     * @param edgeSupplier     The supplier for the custom NetEdge child class.
     * @param algorithmBuilder custom function to construct a new algorithm when the old one is invalidated.
     */
    public WorldPipeNetComplex(String name, boolean isDirected, Supplier<Edge> edgeSupplier,
                               Function<WorldPipeNetBase<NodeDataType, PipeType, Edge>, INetAlgorithm<PipeType, NodeDataType, Edge>> algorithmBuilder) {
        super(name, isDirected, algorithmBuilder, isDirected ? new SimpleDirectedWeightedGraph<>(null, edgeSupplier) :
                new SimpleWeightedGraph<>(null, edgeSupplier));
    }
}
