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
     * Alternate pipenet representation.
     * Supports pipenet-level limits on flow per tick and nodes with multiple channels, but cannot cache paths.
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
                isDirected ? new DirectedLimitedGraph<>(() -> new NetEdge(flowBufferTicks)) :
                        new UndirectedLimitedGraph<>(() -> new NetEdge(flowBufferTicks)));
    }

//    @Override
//    public void addNodeSilent(NodeG<PipeType, NodeDataType> node) {
//        super.addNodeSilent(node);
//        // Flow algorithms will throw an out of index if the number of nodes increases
//        this.markAlgInvalid();
//    }
//
//    @Override
//    public void removeNode(@Nullable NodeG<PipeType, NodeDataType> node) {
//        super.removeNode(node);
//        // Flow algorithms will become mis-indexed if the number of nodes decreases
//        this.markAlgInvalid();
//    }

    @Override
    public List<NetPath<PipeType, NodeDataType>> getPaths(@Nullable NodeG<PipeType, NodeDataType> node,
                                                          @Nullable TileEntityPipeBase<PipeType, NodeDataType> tile,
                                                          Object testObject) {
        if (node == null) return new ObjectArrayList<>();

        node.setHeldMTE(tile);

        if (!this.hasValidAlg()) this.rebuildNetAlgorithm();

        ((IComplexGraph<NodeDataType, PipeType>) this.pipeGraph).setTestObject(testObject);
        List<NetPath<PipeType, NodeDataType>> list = this.netAlgorithm.getPathsList(node);
        ((IComplexGraph<NodeDataType, PipeType>) this.pipeGraph).setTestObject(null);
        return verifyList(list, node);
    }


    public interface IComplexGraph<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>> extends Graph<NodeG<PT, NDT>, NetEdge> {

        void setTestObject(Object object);
    }

    protected static class UndirectedLimitedGraph<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>>
            extends SimpleWeightedGraph<NodeG<PT, NDT>, NetEdge>
            implements IComplexGraph<NDT, PT> {

        final ThreadLocal<Object> testObject = new ThreadLocal<>();

        public UndirectedLimitedGraph(Supplier<NetEdge> supplier) {
            super(null, supplier);
        }

        @Override
        public double getEdgeWeight(NetEdge netEdge) {
            long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            int limit = netEdge.getFlowLimit(testObject.get(), this, tick, null);
            return limit > 0 && netEdge.getPredicate().test(testObject.get()) ? this.getEdgeWeight(netEdge) : Double.POSITIVE_INFINITY;
        }

        @Override
        public void setTestObject(Object object) {
            this.testObject.set(object);
        }
    }

    protected static class DirectedLimitedGraph<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>>
                                       extends SimpleDirectedWeightedGraph<NodeG<PT, NDT>, NetEdge>
            implements IComplexGraph<NDT, PT> {

        final ThreadLocal<Object> testObject = new ThreadLocal<>();


        public DirectedLimitedGraph(Supplier<NetEdge> supplier) {
            super(null, supplier);
        }

        @Override
        public double getEdgeWeight(NetEdge netEdge) {
            long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            int limit = netEdge.getFlowLimit(testObject.get(), this, tick, null);
            return limit > 0 && netEdge.getPredicate().test(testObject.get()) ? super.getEdgeWeight(netEdge) : Double.POSITIVE_INFINITY;
        }

        @Override
        public void setTestObject(Object object) {
            this.testObject.set(object);
        }
    }
}
