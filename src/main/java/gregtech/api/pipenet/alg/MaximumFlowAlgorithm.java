package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.flow.PushRelabelMFImpl;

import java.util.List;
import java.util.Map;

public final class MaximumFlowAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>>
        extends PushRelabelMFImpl<NodeG<PT, NDT>, NetEdge> implements NetAlgorithm<PT, NDT> {

    private final NetAlgorithmWrapper<PT, NDT> wrapper;

    private NodeG<PT, NDT> superSource = new NodeG<>();
    private Map<NodeG<PT, NDT>, Double> activeSources = new Object2DoubleOpenHashMap<>();

    private NodeG<PT, NDT> superSink = new NodeG<>();
    private Map<NodeG<PT, NDT>, Double> activeSinks = new Object2DoubleOpenHashMap<>();


    public MaximumFlowAlgorithm(Graph<NodeG<PT, NDT>, NetEdge> network, NetAlgorithmWrapper<PT, NDT> wrapper) {
        super(network);
        this.wrapper = wrapper;
    }

    @Override
    public List<NetPath<PT, NDT>> getPathsList(NodeG<PT, NDT> source) {
        if (!network.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        List<NetPath<PT, NDT>> paths = new ObjectArrayList<>();
        paths.add(new NetPath<>(source));
        // if the source has no group, it has no paths other than the path to itself.
        if (source.getGroup() == null) return paths;

        for (NodeG<PT, NDT> v : source.getGroup().getNodes()) {
            if (v == source) continue;
            paths.add(new FlowPath<>(wrapper));
        }
        return paths;
    }

    /**
     * Prime the edges to the super nodes to prepare for calculations.
     */
    private void activate() {
        for (Map.Entry<NodeG<PT, NDT>, Double> source : activeSources.entrySet()) {
            network.setEdgeWeight(superSource, source.getKey(), source.getValue());
        }
        for (Map.Entry<NodeG<PT, NDT>, Double> sink : activeSinks.entrySet()) {
            network.setEdgeWeight(superSink, sink.getKey(), sink.getValue());
        }
    }

    /**
     * Zero out the edges to the super nodes to prevent other calculations from using them.
     */
    private void deactivate() {
        for (Map.Entry<NodeG<PT, NDT>, Double> source : activeSources.entrySet()) {
            network.setEdgeWeight(superSource, source.getKey(), 0);
        }
        for (Map.Entry<NodeG<PT, NDT>, Double> sink : activeSinks.entrySet()) {
            network.setEdgeWeight(superSink, sink.getKey(), 0);
        }
    }

    public void setSource(NodeG<PT, NDT> source, double amount) {
        if (amount <= 0) {
            activeSources.remove(source);
            return;
        }
        activeSources.put(source, amount);
    }

    public void setSink(NodeG<PT, NDT> sink, double amount) {
        if (amount <= 0) {
            activeSinks.remove(sink);
            return;
        }
        activeSinks.put(sink, amount);
    }

    public static final class FlowPath<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>> extends NetPath<PipeType, NodeDataType> {

        private final NetAlgorithmWrapper<PipeType, NodeDataType> alg;

        public FlowPath(NetAlgorithmWrapper<PipeType, NodeDataType> alg) {
            super();
            this.alg = alg;
        }

        // this roundabout method is necessary since a new MaximumFlowAlgorithm is created every time the graph changes.
        private MaximumFlowAlgorithm<PipeType, NodeDataType> getAlg() {
            if (alg.getAlg() instanceof MaximumFlowAlgorithm<PipeType,NodeDataType> algorithm) {
                return algorithm;
            } else {
                throw new IllegalStateException("A WorldPipeNetG was changed away from a flow graph during runtime!");
            }
        }
    }
}
