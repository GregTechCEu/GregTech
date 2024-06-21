package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.List;

public final class NetFlowEdge extends AbstractNetFlowEdge<NetFlowEdge> {

    private final int flowBufferTicks;

    /**
     * NetEdge that provides standard flow behavior handling
     *
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                        for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                        throughput.
     */
    public NetFlowEdge(int flowBufferTicks) {
        this.flowBufferTicks = flowBufferTicks;
    }

    @Override
    protected AbstractChannelsHolder<NetFlowEdge> getNewHolder(AbstractChannelsHolder<NetFlowEdge> prototype, ChannelSimulatorKey simulator) {
        if (prototype instanceof ChannelsHolder holder) return new ChannelsHolder(holder, simulator);
        return new ChannelsHolder(simulator);
    }

    private final class ChannelsHolder extends AbstractChannelsHolder<NetFlowEdge> {

        private final Object2IntOpenHashMap<Object> map;
        private long lastQueryTick;

        public ChannelsHolder(ChannelSimulatorKey simulator) {
            super(simulator);
            this.map = new Object2IntOpenHashMap<>(9);
            this.map.defaultReturnValue(getMinData().getThroughput() * flowBufferTicks);
        }

        public ChannelsHolder(ChannelsHolder prototype, ChannelSimulatorKey simulator) {
            super(simulator);
            this.map = prototype.map.clone();
            this.lastQueryTick = prototype.lastQueryTick;
        }

        @Override
        public boolean cannotSupportChannel(Object channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            if (map.containsKey(channel)) return map.getInt(channel) <= 0;
            else return map.size() >= getMinData().getChannelMaxCount();
        }

        @Override
        public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getFlowLimit(Object channel,
                                                                                                   Graph<NetNode<PT, NDT, NetFlowEdge>, NetFlowEdge> graph,
                                                                                                   long queryTick) {
            if (cannotSupportChannel(channel, queryTick)) return 0;
            int limit = map.getInt(channel);

            NetFlowEdge inverse = graph.getEdge(getCastTarget(), getCastSource());
            if (inverse != null && inverse != NetFlowEdge.this) {
                if (inverse.cannotSupportChannel(channel, queryTick, getSimulator())) return 0;
                limit += inverse.getConsumedLimit(channel, queryTick, getSimulator());
            }

            return limit;
        }

        @Override
        int getConsumedLimit(Object channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            int limit = map.defaultReturnValue();
            return limit - map.getInt(channel);
        }

        @Override
        <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                Object channel, Graph<NetNode<PT, NDT, NetFlowEdge>, NetFlowEdge> graph, int amount, long queryTick) {
            if (amount == 0) return;
            recalculateFlowLimits(queryTick);

            // check against reverse edge
            NetFlowEdge inverse = graph.getEdge(getCastTarget(), getCastSource());
            if (inverse != null && inverse != NetFlowEdge.this) {
                int inverseConsumed = inverse.getConsumedLimit(channel, queryTick, getSimulator());
                if (inverseConsumed != 0) {
                    int toFreeUp = Math.min(inverseConsumed, amount);
                    inverse.consumeFlowLimit(channel, graph, -toFreeUp, queryTick, getSimulator());
                    if (toFreeUp == amount) return;
                    amount -= toFreeUp;
                }
            }

            int finalAmount = amount;
            map.compute(channel, (k, v) -> {
                int d = map.defaultReturnValue();
                if (v == null) v = d;
                v -= finalAmount;
                if (v >= d) return null;
                return v;
            });
        }

        @Override
        public void recalculateFlowLimits(long queryTick) {
            int time = (int) (queryTick - this.lastQueryTick);
            if (time < 0) {
                this.map.clear();
            } else {
                List<Object> toRemove = new ObjectArrayList<>();
                this.map.replaceAll((k, v) -> {
                    v += time * getMinData().getThroughput();
                    if (v >= map.defaultReturnValue()) toRemove.add(k);
                    return v;
                });
                toRemove.forEach(this.map::removeInt);
            }
            this.lastQueryTick = queryTick;
        }
    }
}
