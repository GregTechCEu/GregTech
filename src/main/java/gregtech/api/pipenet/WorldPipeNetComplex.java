package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.function.Supplier;

public abstract class WorldPipeNetComplex<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldPipeNetSimple<NodeDataType, PipeType> {

    /**
     * Alternate pipenet representation. Disables node path caching and activates {@link NetEdge} flow behavior.
     * <p>
     * Note - undirected versions of this pipenet will treat flow in either direction along an edge towards its capacity,
     * while directed versions will cancel out reverse flow for improved behavior.
     *
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections
     *                     or unidirectional covers.
     * @param isSinglePath Determines whether this net allows only one source and one destination per group.
     *                     Allows for optimizations in path lookup and cache invalidation.
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges.
     *                      Allows for once-an-interval push/pull operations instead of needing them every tick for
     *                      maximum throughput.
     */
    public WorldPipeNetComplex(String name, boolean isDirected, boolean isSinglePath, int flowBufferTicks) {
        super(name, isDirected, isSinglePath,
                isDirected ? new SimpleDirectedWeightedGraph<>(null, () -> new NetEdge(flowBufferTicks)) :
                        new SimpleWeightedGraph<>(null, () -> new NetEdge(flowBufferTicks)));
    }

    @Override
    public List<NetPath<PipeType, NodeDataType>> getPaths(@Nullable NodeG<PipeType, NodeDataType> node,
                                                          @Nullable TileEntityPipeBase<PipeType, NodeDataType> tile,
                                                          Object testObject) {
        if (node == null) return new ObjectArrayList<>();

        node.setHeldMTE(tile);

        if (!this.hasValidAlg()) this.rebuildNetAlgorithm();

        List<NetPath<PipeType, NodeDataType>> list = this.netAlgorithm.getPathsList(node);
        return verifyList(list, node);
    }
}
