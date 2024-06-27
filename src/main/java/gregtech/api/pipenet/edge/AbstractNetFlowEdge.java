package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.predicate.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class AbstractNetFlowEdge<E extends AbstractNetFlowEdge<E>> extends NetEdge {

    private final AbstractChannelsHolder<E> channels;
    private final WeakHashMap<SimulatorKey, AbstractChannelsHolder<E>> simulatedChannels;

    public AbstractNetFlowEdge() {
        this.channels = getNewHolder(null, null);
        this.simulatedChannels = new WeakHashMap<>(9);
    }

    @Override
    public double getDynamicWeight(IPredicateTestObject channel, SimulatorKey simulator, long queryTick) {
        if (!cannotSupportChannel(channel, queryTick, simulator)) {
            return getWeight();
        }
        return Double.POSITIVE_INFINITY;
    }

    public boolean cannotSupportChannel(Object channel, long queryTick, @Nullable SimulatorKey simulator) {
        return getChannels(simulator).cannotSupportChannel(channel, queryTick);
    }

    protected AbstractChannelsHolder<E> getChannels(@Nullable SimulatorKey simulator) {
        if (simulator == null) return this.channels;
        else {
            AbstractChannelsHolder<E> channels = simulatedChannels.get(simulator);
            if (channels == null) {
                channels = getNewHolder(this.channels, simulator);
                simulatedChannels.put(simulator, channels);
            }
            return channels;
        }
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> long getFlowLimit(
                                                                                                Object channel,
                                                                                                Graph<NetNode<PT, NDT, E>, E> graph,
                                                                                                long queryTick,
                                                                                                @Nullable SimulatorKey simulator) {
        return getChannels(simulator).getFlowLimit(channel, graph, queryTick);
    }

    public long getConsumedLimit(Object channel, long queryTick, @Nullable SimulatorKey simulator) {
        return getChannels(simulator).getConsumedLimit(channel, queryTick);
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                                                                                                    Object channel,
                                                                                                    Graph<NetNode<PT, NDT, E>, E> graph,
                                                                                                    long amount,
                                                                                                    long queryTick,
                                                                                                    @Nullable SimulatorKey simulator) {
        getChannels(simulator).consumeFlowLimit(channel, graph, amount, queryTick);
    }

    public Set<Object> getActiveChannels(@Nullable SimulatorKey simulator, long queryTick) {
        return getChannels(simulator).getActiveChannels(queryTick);
    }

    protected abstract AbstractChannelsHolder<E> getNewHolder(AbstractChannelsHolder<E> prototype,
                                                              SimulatorKey simulator);

    protected abstract static class AbstractChannelsHolder<E extends AbstractNetFlowEdge<E>> {

        private final WeakReference<SimulatorKey> simulator;

        public AbstractChannelsHolder(SimulatorKey simulator) {
            this.simulator = new WeakReference<>(simulator);
        }

        public SimulatorKey getSimulator() {
            return simulator.get();
        }

        abstract void recalculateFlowLimits(long queryTick);

        abstract boolean cannotSupportChannel(Object channel, long queryTick);

        abstract <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> long getFlowLimit(
                                                                                                      Object channel,
                                                                                                      Graph<NetNode<PT, NDT, E>, E> graph,
                                                                                                      long queryTick);

        abstract long getConsumedLimit(Object channel, long queryTick);

        abstract <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                                                                                                          Object channel,
                                                                                                          Graph<NetNode<PT, NDT, E>, E> graph,
                                                                                                          long amount,
                                                                                                          long queryTick);

        abstract Set<Object> getActiveChannels(long queryTick);
    }
}
