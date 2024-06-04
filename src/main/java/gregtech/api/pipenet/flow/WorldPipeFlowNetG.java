package gregtech.api.pipenet.flow;

import gregtech.api.pipenet.AbstractEdgePredicate;
import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetGroup;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class WorldPipeFlowNetG<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldPipeNetG<NodeDataType, PipeType> {

    private final NodeG<PipeType, NodeDataType> superSource = new NodeG<>();
    private final NodeG<PipeType, NodeDataType> superSink = new NodeG<>();

    protected final Set<NodeG<PipeType, NodeDataType>> unhandledOldNodes = new ObjectOpenHashSet<>();

    protected final Set<WeakReference<FlowChannelManager<?, ?>>> managers = new ObjectOpenHashSet<>();

    /**
     * @param isDirected Determines whether this net needs directed graph handling.
     *                   Used to respect filter directions in the item net and fluid net, for example.
     *                   If the graph is not directed, pipes should not support blocked connections
     *                   or unidirectional covers.
     */
    public WorldPipeFlowNetG(String name, boolean isDirected) {
        super(name, isDirected, false, isDirected ? new FlowDirected<>() : new FlowUndirected<>());
        this.pipeGraph.addVertex(superSource);
        this.pipeGraph.addVertex(superSink);
    }

    @Override
    public void markNodeAsOldData(NodeG<PipeType, NodeDataType> node) {
        this.unhandledOldNodes.add(node);
    }

    Graph<NodeG<PipeType, NodeDataType>, NetEdge> getPipeGraph() {
        return this.pipeGraph;
    }

    protected abstract Capability<?> getSinkCapability();

    @Override
    public void addNodeSilent(NodeG<PipeType, NodeDataType> node) {
        super.addNodeSilent(node);
        // Flow algorithms will throw an out of index if the number of nodes increases
        this.markAlgInvalid();
    }

    @Override
    public void removeNode(@Nullable NodeG<PipeType, NodeDataType> node) {
        super.removeNode(node);
        // Flow algorithms will become mis-indexed if the number of nodes decreases
        this.markAlgInvalid();
    }

    @Override
    public void addEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target,
                        @Nullable AbstractEdgePredicate<?> predicate) {
        addEdge(source, target, Math.min(source.getData().getWeightFactor(), target.getData().getWeightFactor()) *
                FlowChannelTicker.FLOWNET_TICKRATE, predicate);
        this.markAlgInvalid();
    }

    @Override
    protected void onWorldSet() {}

    @Override
    protected void markAlgInvalid() {
        Iterator<WeakReference<FlowChannelManager<?, ?>>> iterator = this.managers.iterator();
        while (iterator.hasNext()) {
            WeakReference<FlowChannelManager<?, ?>> ref = iterator.next();
            FlowChannelManager<?, ?> manager = ref.get();
            if (manager != null) manager.clearAlgs();
            else iterator.remove();
        }
    }

    public void addManager(WeakReference<FlowChannelManager<?, ?>> ref) {
        this.managers.add(ref);
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

        final ThreadLocal<Object> testObject = new ThreadLocal<>();
        final ThreadLocal<FlowChannel<PT, NDT>> queryingChannel = new ThreadLocal<>();

        public FlowUndirected() {
            super(NetEdge.class);
        }

        // this overcomplicated workaround is due to not enough protected/public visibilities.
        @Override
        public double getEdgeWeight(NetEdge netEdge) {
            // Both source and target must support the channel, and the netEdge predicate must allow our object.
            return ((NodeG<PT, NDT>) netEdge.getSource()).canSupportChannel(queryingChannel.get()) &&
                    ((NodeG<PT, NDT>) netEdge.getTarget()).canSupportChannel(queryingChannel.get()) &&
                    netEdge.getPredicate().test(testObject.get()) ? super.getEdgeWeight(netEdge) : 0;
        }

        @Override
        public void setTestObject(Object object) {
            this.testObject.set(object);
        }

        @Override
        public void setQueryingChannel(FlowChannel<PT, NDT> channel) {
            this.queryingChannel.set(channel);
        }
    }

    protected static class FlowDirected<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>>
                                       extends SimpleDirectedWeightedGraph<NodeG<PT, NDT>, NetEdge>
                                       implements IFlowGraph<NDT, PT> {

        final ThreadLocal<Object> testObject = new ThreadLocal<>();
        final ThreadLocal<FlowChannel<PT, NDT>> queryingChannel = new ThreadLocal<>();

        public FlowDirected() {
            super(NetEdge.class);
        }

        // this overcomplicated workaround is due to not enough protected/public visibilities.
        @Override
        public double getEdgeWeight(NetEdge netEdge) {
            // Both source and target must support the channel, and the netEdge predicate must allow our object.
            return ((NodeG<PT, NDT>) netEdge.getSource()).canSupportChannel(queryingChannel.get()) &&
                    ((NodeG<PT, NDT>) netEdge.getTarget()).canSupportChannel(queryingChannel.get()) &&
                    netEdge.getPredicate().test(testObject.get()) ? super.getEdgeWeight(netEdge) : 0;
        }

        @Override
        public void setTestObject(Object object) {
            this.testObject.set(object);
        }

        @Override
        public void setQueryingChannel(FlowChannel<PT, NDT> channel) {
            this.queryingChannel.set(channel);
        }
    }
}
