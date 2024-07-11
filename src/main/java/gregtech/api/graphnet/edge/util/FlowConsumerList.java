package gregtech.api.graphnet.edge.util;

import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.graphnet.pipenetold.PipeNetNode;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.FluidTestObject;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.function.Consumer;

public class FlowConsumerList<PT extends Enum<PT> & IPipeType<NDT>, NDT extends IPipeNetData<NDT>,
        E extends AbstractNetFlowEdge> extends ObjectArrayList<FlowConsumer<PT, NDT, E>> {

    public void add(E edge, FluidTestObject testObject, Graph<PipeNetNode<PT, NDT, E>, E> graph, long flow,
                    long tick, SimulatorKey simulatorKey) {
        this.add(new FlowConsumer<>(edge, testObject, graph, flow, tick, simulatorKey));
    }

    public void add(E edge, FluidTestObject testObject, Graph<PipeNetNode<PT, NDT, E>, E> graph, long flow,
                    long tick, SimulatorKey simulatorKey, Consumer<Long> extra) {
        this.add(new FlowConsumer<>(edge, testObject, graph, flow, tick, simulatorKey, extra));
    }

    public void modifyRatios(double ratio) {
        for (FlowConsumer<PT, NDT, E> consumer : this) {
            consumer.modifyRatio(ratio);
        }
    }

    public void doConsumption(double ratio) {
        for (FlowConsumer<PT, NDT, E> consumer : this) {
            consumer.accept(ratio);
        }
    }
}
