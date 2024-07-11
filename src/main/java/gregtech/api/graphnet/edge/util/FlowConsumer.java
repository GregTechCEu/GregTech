package gregtech.api.graphnet.edge.util;

import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.graphnet.pipenetold.PipeNetNode;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.FluidTestObject;

import org.jgrapht.Graph;

import java.util.function.Consumer;

public class FlowConsumer<PT extends Enum<PT> & IPipeType<NDT>, NDT extends IPipeNetData<NDT>,
        E extends AbstractNetFlowEdge> implements Consumer<Double> {

    private final E edge;
    private final FluidTestObject testObject;
    private final Graph<PipeNetNode<PT, NDT, E>, E> graph;
    private final long flow;
    private final long tick;
    private final SimulatorKey simulatorKey;
    private final Consumer<Long> extra;

    private double ratio = 1;

    public FlowConsumer(E edge, FluidTestObject testObject, Graph<PipeNetNode<PT, NDT, E>, E> graph, long flow,
                        long tick, SimulatorKey simulatorKey) {
        this(edge, testObject, graph, flow, tick, simulatorKey, null);
    }

    public FlowConsumer(E edge, FluidTestObject testObject, Graph<PipeNetNode<PT, NDT, E>, E> graph, long flow,
                        long tick, SimulatorKey simulatorKey, Consumer<Long> extra) {
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
