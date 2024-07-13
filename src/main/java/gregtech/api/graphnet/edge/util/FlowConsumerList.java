package gregtech.api.graphnet.edge.util;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class FlowConsumerList extends ObjectArrayList<FlowConsumer> {

    public void add(AbstractNetFlowEdge edge, IPredicateTestObject testObject, IGraphNet graph, long flow,
                    long tick, SimulatorKey simulatorKey) {
        this.add(new FlowConsumer(edge, testObject, graph, flow, tick, simulatorKey));
    }

    public void add(AbstractNetFlowEdge edge, IPredicateTestObject testObject, IGraphNet graph, long flow,
                    long tick, SimulatorKey simulatorKey, Consumer<Long> extra) {
        this.add(new FlowConsumer(edge, testObject, graph, flow, tick, simulatorKey, extra));
    }

    public void modifyRatios(double ratio) {
        for (FlowConsumer consumer : this) {
            consumer.modifyRatio(ratio);
        }
    }

    public void modifyReductions(long reduction) {
        for (FlowConsumer consumer : this) {
            consumer.modifyReduction(reduction);
        }
    }

    public void modifyArbitrary(UnaryOperator<Double> modifier) {
        for (FlowConsumer consumer : this) {
            consumer.modifyArbitrary(modifier);
        }
    }

    public void doConsumption(double finalRatio) {
        for (FlowConsumer consumer : this) {
            consumer.finalRatio(finalRatio);
        }
    }

    public void doConsumption(long finalReduction) {
        for (FlowConsumer consumer : this) {
            consumer.finalReduction(finalReduction);
        }
    }

    public void doConsumption(UnaryOperator<Double> finalModifier) {
        for (FlowConsumer consumer : this) {
            consumer.finalArbitrary(finalModifier);
        }
    }

    public void doConsumption() {
        for (FlowConsumer consumer : this) {
            consumer.consume();
        }
    }
}
