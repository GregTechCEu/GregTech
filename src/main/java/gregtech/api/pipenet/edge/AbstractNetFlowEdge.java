package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public abstract class AbstractNetFlowEdge<E extends AbstractNetFlowEdge<E>> extends NetEdge {

    private final AbstractChannelsHolder<E> channels;
    private final WeakHashMap<ChannelSimulatorKey, AbstractChannelsHolder<E>> simulatedChannels;

    public AbstractNetFlowEdge() {
        this.channels = getNewHolder(null, null);
        this.simulatedChannels = new WeakHashMap<>(9);
    }

    /**
     * Claims a new, unique simulator instance for properly simulating flow edge limits without actually changing them.
     * <br>
     * This simulator must be discarded after use so that the garbage collector can clean up.
     */
    public static ChannelSimulatorKey getNewSimulatorInstance() {
        return new ChannelSimulatorKey();
    }


    public boolean cannotSupportChannel(Object channel, long queryTick, @Nullable ChannelSimulatorKey simulator) {
        return getChannels(simulator).cannotSupportChannel(channel, queryTick);
    }

    protected AbstractChannelsHolder<E> getChannels(@Nullable ChannelSimulatorKey simulator) {
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

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getFlowLimit(
            Object channel,
            Graph<NetNode<PT, NDT, E>, E> graph,
            long queryTick,
            @Nullable ChannelSimulatorKey simulator) {
        return getChannels(simulator).getFlowLimit(channel, graph, queryTick);
    }

    public int getConsumedLimit(Object channel, long queryTick, @Nullable ChannelSimulatorKey simulator) {
        return getChannels(simulator).getConsumedLimit(channel, queryTick);
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
            Object channel,
            Graph<NetNode<PT, NDT, E>, E> graph,
            int amount, long queryTick,
            @Nullable ChannelSimulatorKey simulator) {
        getChannels(simulator).consumeFlowLimit(channel, graph, amount, queryTick);
    }

    protected abstract AbstractChannelsHolder<E> getNewHolder(AbstractChannelsHolder<E> prototype, ChannelSimulatorKey simulator);

    protected abstract static class AbstractChannelsHolder<E extends AbstractNetFlowEdge<E>> {

        private final WeakReference<ChannelSimulatorKey> simulator;

        public AbstractChannelsHolder(ChannelSimulatorKey simulator) {
            this.simulator = new WeakReference<>(simulator);
        }

        public ChannelSimulatorKey getSimulator() {
            return simulator.get();
        }

        abstract void recalculateFlowLimits(long queryTick);

        abstract boolean cannotSupportChannel(Object channel, long queryTick);

        abstract <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getFlowLimit(
                Object channel,
                Graph<NetNode<PT, NDT, E>, E> graph,
                long queryTick);

        abstract int getConsumedLimit(Object channel, long queryTick);

        abstract <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                Object channel,
                Graph<NetNode<PT, NDT, E>, E> graph,
                int amount, long queryTick);
    }

    public static final class ChannelSimulatorKey {

        private static int ID;
        private final int id;

        private ChannelSimulatorKey() {
            this.id = ID++;
        }

        @Override
        public int hashCode() {
            // enforcing hash uniqueness improves weak map performance
            return id;
        }
    }
}
