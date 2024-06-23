package gregtech.api.pipenet.edge.util;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.AbstractNetFlowEdge;
import gregtech.api.util.FluidTestObject;

import org.jgrapht.Graph;

import java.util.function.Consumer;

public class FlowConsumer<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
        E extends AbstractNetFlowEdge<E>> implements Consumer<Double> {

    private final E edge;
    private final FluidTestObject testObject;
    private final Graph<NetNode<PT, NDT, E>, E> graph;
    private final long flow;
    private final long tick;
    private final AbstractNetFlowEdge.ChannelSimulatorKey simulatorKey;
    private final Consumer<Long> extra;

    private double ratio = 1;

    public FlowConsumer(E edge, FluidTestObject testObject, Graph<NetNode<PT, NDT, E>, E> graph, long flow,
                        long tick, AbstractNetFlowEdge.ChannelSimulatorKey simulatorKey) {
        this(edge, testObject, graph, flow, tick, simulatorKey, null);
    }

    public FlowConsumer(E edge, FluidTestObject testObject, Graph<NetNode<PT, NDT, E>, E> graph, long flow,
                        long tick, AbstractNetFlowEdge.ChannelSimulatorKey simulatorKey, Consumer<Long> extra) {
        this.edge = edge;
        this.testObject = testObject;
        this.graph = graph;
        this.flow = flow;
        this.tick = tick;
        this.simulatorKey = simulatorKey;
        this.extra = extra;
    }

    public void modifyRatio(Double ratio) {
        this.ratio *= ratio;
    }

    @Override
    public void accept(Double finalRatio) {
        long consumption = (long) (finalRatio * ratio * flow);
        edge.consumeFlowLimit(testObject, graph, consumption, tick, simulatorKey);
        if (extra != null) extra.accept(consumption);
    }
}
