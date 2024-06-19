package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.List;
import java.util.WeakHashMap;

public class NetFlowEdge extends NetEdge implements INetFlowEdge<NetFlowEdge> {

    private final int flowBufferTicks;
    private final ChannelsHolder channels;
    private final WeakHashMap<INetFlowEdge.ChannelSimulatorKey, ChannelsHolder> simulatedChannels;

    private INodeData<? extends INodeData<?>> minData;

    /**
     * NetEdge that provides standard flow behavior handling
     *
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                        for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                        throughput.
     */
    public NetFlowEdge(int flowBufferTicks) {
        this.flowBufferTicks = flowBufferTicks;
        this.channels = new ChannelsHolder();
        this.simulatedChannels = new WeakHashMap<>(9);
    }

    private ChannelsHolder getChannels(@Nullable INetFlowEdge.ChannelSimulatorKey simulator) {
        if (simulator == null) return this.channels;
        else {
            ChannelsHolder channels = simulatedChannels.get(simulator);
            if (channels == null) {
                channels = new ChannelsHolder(this.channels);
                simulatedChannels.put(simulator, channels);
            }
            return channels;
        }
    }

    private INodeData<? extends INodeData<?>> getMinData() {
        if (this.minData == null)
            this.minData = this.getCastSource().getData().getMinData(this.getCastTarget().getData());
        return this.minData;
    }

    private int getAdjustedThroughput() {
        return getMinData().getThroughput() * flowBufferTicks;
    }

    private boolean cannotSupportChannel(Object channel, long queryTick,
                                         @Nullable INetFlowEdge.ChannelSimulatorKey simulator) {
        var channels = getChannels(simulator);
        channels.recalculateFlowLimits(queryTick);
        return channels.map.size() >= getMinData().getChannelMaxCount() && !channels.map.containsKey(channel);
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getFlowLimit(
                                                                                               Object channel,
                                                                                               Graph<NetNode<PT, NDT, NetFlowEdge>, NetFlowEdge> graph,
                                                                                               long queryTick,
                                                                                               @Nullable INetFlowEdge.ChannelSimulatorKey simulator) {
        if (this.cannotSupportChannel(channel, queryTick, simulator)) {
            return 0;
        }
        int limit = getChannels(simulator).map.getOrDefault(channel, getAdjustedThroughput());

        NetFlowEdge inverse = graph.getEdge(this.getCastTarget(), this.getCastSource());
        if (inverse != null && inverse != this) {
            if (inverse.cannotSupportChannel(channel, queryTick, simulator)) return 0;
            limit += inverse.getConsumedLimit(channel, queryTick, simulator);
        }

        return limit;
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getConsumedLimit(
                                                                                                   Object channel,
                                                                                                   long queryTick,
                                                                                                   @Nullable INetFlowEdge.ChannelSimulatorKey simulator) {
        var channels = getChannels(simulator);
        channels.recalculateFlowLimits(queryTick);
        int limit = getAdjustedThroughput();
        return limit - channels.map.getOrDefault(channel, limit);
    }

    @Override
    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                                                                                                    Object channel,
                                                                                                    Graph<NetNode<PT, NDT, NetFlowEdge>, NetFlowEdge> graph,
                                                                                                    int amount,
                                                                                                    long queryTick,
                                                                                                    @Nullable INetFlowEdge.ChannelSimulatorKey simulator) {
        if (amount == 0) return;
        var channels = getChannels(simulator);
        channels.recalculateFlowLimits(queryTick);

        // check against reverse edge
        NetFlowEdge inverse = graph.getEdge(this.getCastTarget(), this.getCastSource());
        if (inverse != null && inverse != this) {
            int inverseConsumed = inverse.getConsumedLimit(channel, queryTick, simulator);
            if (inverseConsumed != 0) {
                int toFreeUp = Math.min(inverseConsumed, amount);
                inverse.consumeFlowLimit(channel, graph, -toFreeUp, queryTick, simulator);
                if (toFreeUp == amount) return;
                amount -= toFreeUp;
            }
        }

        int finalAmount = amount;
        channels.map.compute(channel, (k, v) -> {
            int d = getAdjustedThroughput();
            if (v == null) v = d;
            v -= finalAmount;
            if (v >= d) return null;
            return v;
        });
    }

    private final class ChannelsHolder {

        public final Object2IntOpenHashMap<Object> map;
        public long lastQueryTick;

        public ChannelsHolder() {
            this.map = new Object2IntOpenHashMap<>(9);
        }

        public ChannelsHolder(ChannelsHolder prototype) {
            this.map = prototype.map.clone();
            this.lastQueryTick = prototype.lastQueryTick;
        }

        public void recalculateFlowLimits(long queryTick) {
            int time = (int) (queryTick - this.lastQueryTick);
            if (time < 0) {
                this.map.clear();
            } else {
                List<Object> toRemove = new ObjectArrayList<>();
                this.map.replaceAll((k, v) -> {
                    v += time * getMinData().getThroughput();
                    if (v >= getAdjustedThroughput()) toRemove.add(k);
                    return v;
                });
                toRemove.forEach(this.map::removeInt);
            }
            this.lastQueryTick = queryTick;
        }
    }
}
