package gregtech.api.pipenet.edge.util;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.AbstractNetFlowEdge;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.util.FluidTestObject;

import org.jgrapht.Graph;

import java.util.function.Consumer;

public class FlowConsumer<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
        E extends AbstractNetFlowEdge> implements Consumer<Double> {

    private final AbstractNetFlowEdge edge;
    private final FluidTestObject testObject;
    private final Graph<NetNode<PT, NDT, E>, E> graph;
    private final int flow;
    private final long tick;
    private final AbstractNetFlowEdge.ChannelSimulatorKey simulatorKey;

    private double ratio = 1;

    public FlowConsumer(AbstractNetFlowEdge edge, FluidTestObject testObject, Graph<NetNode<PT, NDT, E>, E> graph, int flow,
                        long tick, AbstractNetFlowEdge.ChannelSimulatorKey simulatorKey) {
        this.edge = edge;
        this.testObject = testObject;
        this.graph = graph;
        this.flow = flow;
        this.tick = tick;
        this.simulatorKey = simulatorKey;
    }

    public void modifyRatio(Double ratio) {
        this.ratio *= ratio;
    }

    @Override
    public void accept(Double finalRatio) {
        edge.consumeFlowLimit(testObject, graph, (int) (finalRatio * ratio * flow), tick, simulatorKey);
    }
}
