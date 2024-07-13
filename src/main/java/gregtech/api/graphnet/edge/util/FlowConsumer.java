package gregtech.api.graphnet.edge.util;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class FlowConsumer {

    private final AbstractNetFlowEdge edge;
    private final IPredicateTestObject testObject;
    private final IGraphNet graph;
    private double flow;
    private final long tick;
    private final SimulatorKey simulatorKey;
    private final Consumer<Long> extra;

    public FlowConsumer(AbstractNetFlowEdge edge, IPredicateTestObject testObject, IGraphNet graph, long flow,
                        long tick, SimulatorKey simulatorKey) {
        this(edge, testObject, graph, flow, tick, simulatorKey, null);
    }

    public FlowConsumer(AbstractNetFlowEdge edge, IPredicateTestObject testObject, IGraphNet graph, long flow,
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
        this.flow *= ratio;
    }

    public void modifyReduction(Long reduction) {
        this.flow -= reduction;
    }

    public void modifyArbitrary(UnaryOperator<Double> modification) {
        this.flow = modification.apply(this.flow);
    }

    public void finalRatio(Double finalRatio) {
        modifyRatio(finalRatio);
        consume();
    }

    public void finalReduction(Long finalReduction) {
        modifyReduction(finalReduction);
        consume();
    }

    public void finalArbitrary(UnaryOperator<Double> modification) {
        modifyArbitrary(modification);
        consume();
    }

    public void consume() {
        long consumption = (long) Math.ceil(this.flow);
        if (consumption > 0) {
            edge.consumeFlowLimit(testObject, graph, consumption, tick, simulatorKey);
            if (extra != null) extra.accept(consumption);
        }
    }
}
