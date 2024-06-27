package gregtech.api.pipenet.edge.util;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.AbstractNetFlowEdge;
import gregtech.api.pipenet.edge.SimulatorKey;
import gregtech.api.pipenet.predicate.FluidTestObject;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.function.Consumer;

public class FlowConsumerList<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
        E extends AbstractNetFlowEdge<E>> extends ObjectArrayList<FlowConsumer<PT, NDT, E>> {

    public void add(E edge, FluidTestObject testObject, Graph<NetNode<PT, NDT, E>, E> graph, long flow,
                    long tick, SimulatorKey simulatorKey) {
        this.add(new FlowConsumer<>(edge, testObject, graph, flow, tick, simulatorKey));
    }

    public void add(E edge, FluidTestObject testObject, Graph<NetNode<PT, NDT, E>, E> graph, long flow,
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
