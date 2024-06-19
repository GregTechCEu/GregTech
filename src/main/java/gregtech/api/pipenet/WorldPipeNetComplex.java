package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.function.Supplier;

public abstract class WorldPipeNetComplex<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        E extends NetEdge> extends WorldPipeNetBase<NodeDataType, PipeType, E> {

    /**
     * Alternate pipenet representation. Allows for using children of the {@link NetEdge} class as edges.
     * <p>
     * Note - undirected versions of this pipenet will treat flow in either direction along an edge towards its
     * capacity,
     * while directed versions will cancel out reverse flow for improved behavior.
     *
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections
     *                     or unidirectional covers.
     * @param isSinglePath Determines whether this net allows only one source and one destination per group.
     *                     Allows for optimizations in path lookup and cache invalidation.
     * @param edgeSupplier The supplier for the custom NetEdge child class.
     */
    public WorldPipeNetComplex(String name, boolean isDirected, boolean isSinglePath, Supplier<E> edgeSupplier) {
        super(name, isDirected, isSinglePath,
                isDirected ? new SimpleDirectedWeightedGraph<>(null, edgeSupplier) :
                        new SimpleWeightedGraph<>(null, edgeSupplier));
    }
}
