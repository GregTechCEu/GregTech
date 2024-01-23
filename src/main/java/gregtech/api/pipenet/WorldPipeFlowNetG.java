package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.Set;

public abstract class WorldPipeFlowNetG<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldPipeNetG<NodeDataType, PipeType> {

    private final NodeG<PipeType, NodeDataType> superSource = new NodeG<>();
    private final NodeG<PipeType, NodeDataType> superSink = new NodeG<>();

    /**
     * @param isDirected Determines whether this net needs directed graph handling.
     *                   Used to respect filter directions in the item net and fluid net, for example.
     *                   If the graph is not directed, pipes should not support blocked connections.
     */
    public WorldPipeFlowNetG(String name, boolean isDirected) {
        super(isDirected, false, name);
        if (isDirected())
            this.pipeGraph = new FlowDirected<>();
        else this.pipeGraph = new FlowUndirected<>();
        this.pipeGraph.addVertex(superSource);
        this.pipeGraph.addVertex(superSink);
    }

    @Override
    public List<NetPath<PipeType, NodeDataType>> getPaths(@Nullable NodeG<PipeType, NodeDataType> node,
                                                          @Nullable TileEntityPipeBase<PipeType, NodeDataType> tile) {
        throw new IllegalStateException("Cannot get paths from a flow network. " +
                "Must locally instantiate algorithm and evaluate; look at the FluidChannel class as an example.");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList allPipeNodes = new NBTTagList();
        Set<NetGroup<PipeType, NodeDataType>> groups = new ObjectOpenHashSet<>();
        for (NodeG<PipeType, NodeDataType> node : pipeGraph.vertexSet()) {
            // prevent contamination by our supernodes
            if (node == superSource || node == superSink) continue;
            if (node.getGroupUnsafe() != null) groups.add(node.getGroupUnsafe());
            NBTTagCompound nodeTag = node.serializeNBT();
            NBTTagCompound dataTag = new NBTTagCompound();
            writeNodeData(node.getData(), dataTag);
            nodeTag.setTag("Data", dataTag);
            allPipeNodes.appendTag(nodeTag);
        }
        compound.setTag("PipeNodes", allPipeNodes);

        NBTTagList allNetEdges = new NBTTagList();
        for (NetEdge edge : pipeGraph.edgeSet()) {
            // prevent contamination by our manager edges
            if (edge.getSource() == superSource || edge.getTarget() == superSource ||
                    edge.getSource() == superSink || edge.getTarget() == superSink)
                continue;
            allNetEdges.appendTag(edge.serializeNBT());
        }
        compound.setTag("NetEdges", allNetEdges);

        NBTTagList allNetGroups = new NBTTagList();
        for (NetGroup<PipeType, NodeDataType> group : groups) {
            allNetGroups.appendTag(group.serializeNBT());
        }
        compound.setTag("NetGroups", allNetGroups);

        return compound;
    }

    public NodeG<PipeType, NodeDataType> getSuperSource() {
        return superSource;
    }

    public NodeG<PipeType, NodeDataType> getSuperSink() {
        return superSink;
    }

    public interface IFlowGraph<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>> {

        void setTestObject(Object object);

        void setQueryingChannel(FlowChannel<PT, NDT> channel);
    }

    protected static class FlowUndirected<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>>
                                         extends SimpleWeightedGraph<NodeG<PT, NDT>, NetEdge>
                                         implements IFlowGraph<NDT, PT> {

        Object testObject;
        FlowChannel<PT, NDT> queryingChannel;

        public FlowUndirected() {
            super(NetEdge.class);
        }

        // this overcomplicated workaround is due to not enough protected/public visibilities.
        @Override
        public double getEdgeWeight(NetEdge netEdge) {
            return netEdge.getPredicate().test(testObject) ? super.getEdgeWeight(netEdge) : 0;
        }

        @Override
        public void setTestObject(Object object) {
            this.testObject = object;
        }

        @Override
        public void setQueryingChannel(FlowChannel<PT, NDT> channel) {
            this.queryingChannel = channel;
        }
    }

    protected static class FlowDirected<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>>
                                       extends SimpleDirectedWeightedGraph<NodeG<PT, NDT>, NetEdge>
                                       implements IFlowGraph<NDT, PT> {

        Object testObject;
        FlowChannel<PT, NDT> queryingChannel;

        public FlowDirected() {
            super(NetEdge.class);
        }

        // this overcomplicated workaround is due to not enough protected/public visibilities.
        @Override
        public double getEdgeWeight(NetEdge netEdge) {
            // Both source and target must support the channel, and the netEdge predicate must allow our object.
            return ((NodeG<PT, NDT>) netEdge.getSource()).canSupportChannel(queryingChannel) &&
                    ((NodeG<PT, NDT>) netEdge.getTarget()).canSupportChannel(queryingChannel) &&
                    netEdge.getPredicate().test(testObject) ? super.getEdgeWeight(netEdge) : 0;
        }

        @Override
        public void setTestObject(Object object) {
            this.testObject = object;
        }

        @Override
        public void setQueryingChannel(FlowChannel<PT, NDT> channel) {
            this.queryingChannel = channel;
        }
    }
}
