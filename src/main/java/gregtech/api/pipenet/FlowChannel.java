package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.util.EnumFacing;

import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;

public abstract class FlowChannel<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> {

    protected final Graph<NodeG<PT, NDT>, NetEdge> network;

    protected NodeG<PT, NDT> superSource = new NodeG<>();
    protected Set<NodeG<PT, NDT>> activeSources = new ObjectOpenHashSet<>();

    protected NodeG<PT, NDT> superSink = new NodeG<>();
    protected Set<NodeG<PT, NDT>> activeSinks = new ObjectOpenHashSet<>();

    protected Map<NodeG<PT, NDT>, Byte> receiveSidesMap = new Object2ObjectOpenHashMap<>();

    public FlowChannel(Graph<NodeG<PT, NDT>, NetEdge> network) {
        this.network = network;
    }

    public abstract void evaluate();

    /**
     * Prime the edges to the super nodes to prepare for calculations.
     */
    protected void activate() {
        for (NodeG<PT, NDT> source : activeSources) {
            double v = getSourceValue(source);
            network.setEdgeWeight(superSource, source, v);
            if (v == 0) removeSource(source);
        }
        for (NodeG<PT, NDT> sink : activeSinks) {
            double v = getSinkValue(sink);
            network.setEdgeWeight(sink, superSink, v);
            if (v == 0) removeSink(sink);
        }
    }

    /**
     * Zero out the edges to the super nodes to prevent other calculations from using them.
     */
    protected void deactivate() {
        for (NodeG<PT, NDT> source : activeSources) {
            network.setEdgeWeight(superSource, source, 0);
        }
        for (NodeG<PT, NDT> sink : activeSinks) {
            network.setEdgeWeight(sink, superSink, 0);
        }
    }

    protected abstract double getSourceValue(NodeG<PT, NDT> source);
    protected abstract double getSinkValue(NodeG<PT, NDT> sink);

    public void addSource(NodeG<PT, NDT> source) {
        this.activeSources.add(source);
        this.network.addEdge(this.superSource, source);
    }

    public void addReceiveSide(NodeG<PT, NDT> node, EnumFacing side) {
        this.receiveSidesMap.compute(node, (k, v) -> {
            if (v == null) {
                byte a = 0;
                a |= (1 << side.getIndex());
                return a;
            }
            byte a = v;
            a |= (1 << side.getIndex());
            return a;
        });
    }

    public void removeSource(NodeG<PT, NDT> source) {
        this.activeSources.remove(source);
        this.network.removeEdge(this.superSource, source);
    }

    public void addSink(NodeG<PT, NDT> sink) {
        this.activeSources.add(sink);
        this.network.addEdge(sink, this.superSink);
    }

    public void removeSink(NodeG<PT, NDT> sink) {
        this.activeSources.remove(sink);
        this.network.removeEdge(sink, this.superSink);
    }
}
