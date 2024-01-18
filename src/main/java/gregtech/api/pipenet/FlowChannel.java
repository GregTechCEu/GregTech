package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.function.Function;

public abstract class FlowChannel<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> {

    protected final Graph<NodeG<PT, NDT>, NetEdge> network;

    protected NodeG<PT, NDT> superSource = new NodeG<>();
    protected Map<NodeG<PT, NDT>, Double> activeSources = new Object2DoubleOpenHashMap<>();

    protected NodeG<PT, NDT> superSink = new NodeG<>();
    protected Map<NodeG<PT, NDT>, Double> activeSinks = new Object2DoubleOpenHashMap<>();

    public FlowChannel(Graph<NodeG<PT, NDT>, NetEdge> network) {
        this.network = network;
    }

    public abstract void evaluate();

    /**
     * Prime the edges to the super nodes to prepare for calculations.
     */
    protected void activate() {
        for (Map.Entry<NodeG<PT, NDT>, Double> source : activeSources.entrySet()) {
            network.setEdgeWeight(superSource, source.getKey(), source.getValue());
        }
        for (Map.Entry<NodeG<PT, NDT>, Double> sink : activeSinks.entrySet()) {
            network.setEdgeWeight(sink.getKey(), superSink, sink.getValue());
        }
    }

    /**
     * Zero out the edges to the super nodes to prevent other calculations from using them.
     */
    protected void deactivate() {
        for (Map.Entry<NodeG<PT, NDT>, Double> source : activeSources.entrySet()) {
            network.setEdgeWeight(superSource, source.getKey(), 0);
        }
        for (Map.Entry<NodeG<PT, NDT>, Double> sink : activeSinks.entrySet()) {
            network.setEdgeWeight(sink.getKey(), superSink, 0);
        }
    }

    public void adjustSource(NodeG<PT, NDT> source, Function<Double, Double> adjuster) {
        activeSources.compute(source, (k, v) -> (v == null) ? adjuster.apply(0d) : adjuster.apply(v));
        if (activeSources.get(source) <= 0) activeSources.remove(source);
    }

    public void adjustSink(NodeG<PT, NDT> sink, Function<Double, Double> adjuster) {
        activeSinks.compute(sink, (k, v) -> (v == null) ? adjuster.apply(0d) : adjuster.apply(v));
        if (activeSinks.get(sink) <= 0) activeSinks.remove(sink);
    }
}
