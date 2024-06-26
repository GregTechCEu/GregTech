package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.List;
import java.util.Set;

public class NetFlowEdge extends AbstractNetFlowEdge<NetFlowEdge> {

    private final int flowBufferTicks;

    /**
     * NetEdge that provides standard flow behavior handling
     *
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                        for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                        throughput.
     */
    public NetFlowEdge(int flowBufferTicks) {
        this.flowBufferTicks = Math.max(flowBufferTicks, 1);
    }

    @Override
    protected AbstractChannelsHolder<NetFlowEdge> getNewHolder(AbstractChannelsHolder<NetFlowEdge> prototype,
                                                               ChannelSimulatorKey simulator) {
        if (prototype instanceof ChannelsHolder holder) return new ChannelsHolder(holder, simulator);
        return new ChannelsHolder(simulator);
    }

    private final class ChannelsHolder extends AbstractChannelsHolder<NetFlowEdge> {

        private final Object2LongOpenHashMap<Object> map;
        private long lastQueryTick;
        private boolean init;

        public ChannelsHolder(ChannelSimulatorKey simulator) {
            super(simulator);
            this.map = new Object2LongOpenHashMap<>(9);
        }

        public ChannelsHolder(ChannelsHolder prototype, ChannelSimulatorKey simulator) {
            super(simulator);
            this.map = prototype.map.clone();
            this.lastQueryTick = prototype.lastQueryTick;
        }

        @Override
        public boolean cannotSupportChannel(Object channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            if (map.containsKey(channel)) return map.getLong(channel) <= 0;
            else return map.size() >= getMinData().getChannelMaxCount();
        }

        @Override
        public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> long getFlowLimit(Object channel,
                                                                                                    Graph<NetNode<PT, NDT, NetFlowEdge>, NetFlowEdge> graph,
                                                                                                    long queryTick) {
            if (cannotSupportChannel(channel, queryTick)) return 0;
            long limit = map.getLong(channel);

            NetFlowEdge inverse = graph.getEdge(getCastTarget(), getCastSource());
            if (inverse != null && inverse != NetFlowEdge.this) {
                if (inverse.cannotSupportChannel(channel, queryTick, getSimulator())) return 0;
                limit += inverse.getConsumedLimit(channel, queryTick, getSimulator());
            }

            return limit;
        }

        @Override
        long getConsumedLimit(Object channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            long limit = map.defaultReturnValue();
            return limit - map.getLong(channel);
        }

        @Override
        <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                                                                                                 Object channel,
                                                                                                 Graph<NetNode<PT, NDT, NetFlowEdge>, NetFlowEdge> graph,
                                                                                                 long amount,
                                                                                                 long queryTick) {
            if (amount == 0) return;
            recalculateFlowLimits(queryTick);

            // check against reverse edge
            NetFlowEdge inverse = graph.getEdge(getCastTarget(), getCastSource());
            if (inverse != null && inverse != NetFlowEdge.this) {
                long inverseConsumed = inverse.getConsumedLimit(channel, queryTick, getSimulator());
                if (inverseConsumed != 0) {
                    long toFreeUp = Math.min(inverseConsumed, amount);
                    inverse.consumeFlowLimit(channel, graph, -toFreeUp, queryTick, getSimulator());
                    if (toFreeUp == amount) return;
                    amount -= toFreeUp;
                }
            }

            long finalAmount = amount;
            map.compute(channel, (k, v) -> {
                long d = map.defaultReturnValue();
                if (v == null) v = d;
                v -= finalAmount;
                if (v >= d) return null;
                return v;
            });
        }

        @Override
        public void recalculateFlowLimits(long queryTick) {
            if (!this.init) {
                this.map.defaultReturnValue((long) getMinData().getThroughput() * flowBufferTicks);
                this.init = true;
            }
            int time = (int) (queryTick - this.lastQueryTick);
            if (time < 0) {
                this.map.clear();
            } else {
                List<Object> toRemove = new ObjectArrayList<>();
                this.map.replaceAll((k, v) -> {
                    v += (long) time * getMinData().getThroughput();
                    if (v >= map.defaultReturnValue()) toRemove.add(k);
                    return v;
                });
                toRemove.forEach(this.map::removeLong);
            }
            this.lastQueryTick = queryTick;
        }

        @Override
        Set<Object> getActiveChannels(long queryTick) {
            recalculateFlowLimits(queryTick);
            return map.keySet();
        }
    }
}
