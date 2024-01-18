package gregtech.api.pipenet;

import gregtech.api.pipenet.alg.MaximumFlowAlgorithm;
import gregtech.api.pipenet.block.IPipeType;

import net.minecraftforge.fluids.Fluid;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

public abstract class WorldPipeFlowNetG<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldPipeNetG<NodeDataType, PipeType> {

    /**
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections.
     */
    public WorldPipeFlowNetG(String name, boolean isDirected) {
        super(name, isDirected, false);
        if (isDirected())
            this.pipeGraph = new FlowDirected<>();
        else this.pipeGraph = new FlowUndirected<>();
    }

    @Override
    protected void rebuildNetAlgorithm() {
        this.netAlgorithm.setAlg(new MaximumFlowAlgorithm<>(pipeGraph));
        this.validAlgorithmInstance = true;
    }

    public interface IFlowGraph<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>> {

        void setTestObject(Object object);

        void setQueryingChannel(FlowChannel<PT, NDT> channel);
    }

    protected static class FlowUndirected<NDT extends INodeData<NDT>, PT extends Enum<PT> & IPipeType<NDT>>
            extends SimpleWeightedGraph<NodeG<PT, NDT>, NetEdge> implements IFlowGraph<NDT, PT> {

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
            extends SimpleDirectedWeightedGraph<NodeG<PT, NDT>, NetEdge> implements IFlowGraph<NDT, PT> {

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
