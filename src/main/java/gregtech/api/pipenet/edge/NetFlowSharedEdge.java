package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;

import net.minecraft.util.math.MathHelper;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.List;

public class NetFlowSharedEdge extends AbstractNetFlowEdge<NetFlowSharedEdge> {

    private final int flowBufferTicks;

    /**
     * NetEdge that provides flow behavior where the capacity along an edge is shared by all channels.
     *
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                        for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                        throughput.
     */
    public NetFlowSharedEdge(int flowBufferTicks) {
        this.flowBufferTicks = Math.max(flowBufferTicks, 1);
    }

    @Override
    protected AbstractChannelsHolder<NetFlowSharedEdge> getNewHolder(AbstractChannelsHolder<NetFlowSharedEdge> prototype,
                                                                     ChannelSimulatorKey simulator) {
        if (prototype instanceof ChannelsHolder holder) return new ChannelsHolder(holder, simulator);
        return new ChannelsHolder(simulator);
    }

    private final class ChannelsHolder extends AbstractChannelsHolder<NetFlowSharedEdge> {

        private long maxCapacity;
        private long sharedCapacity;
        private final Object2LongOpenHashMap<Object> map;
        private long lastQueryTick;
        private boolean init;

        public ChannelsHolder(ChannelSimulatorKey simulator) {
            super(simulator);
            this.map = new Object2LongOpenHashMap<>(9);
            this.map.defaultReturnValue(0);
        }

        public ChannelsHolder(ChannelsHolder prototype, ChannelSimulatorKey simulator) {
            super(simulator);
            this.map = prototype.map.clone();
            this.lastQueryTick = prototype.lastQueryTick;
        }

        @Override
        public boolean cannotSupportChannel(Object channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            if (sharedCapacity <= 0) return true;
            else return map.size() >= getMinData().getChannelMaxCount();
        }

        @Override
        public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> long getFlowLimit(Object channel,
                                                                                                    Graph<NetNode<PT, NDT, NetFlowSharedEdge>, NetFlowSharedEdge> graph,
                                                                                                    long queryTick) {
            if (cannotSupportChannel(channel, queryTick)) return 0;

            NetFlowSharedEdge inverse = graph.getEdge(getCastTarget(), getCastSource());
            if (inverse != null && inverse != NetFlowSharedEdge.this) {
                if (inverse.cannotSupportChannel(channel, queryTick, getSimulator())) return 0;
                return sharedCapacity + inverse.getConsumedLimit(channel, queryTick, getSimulator());
            } else return sharedCapacity;
        }

        @Override
        long getConsumedLimit(Object channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            return map.getLong(channel);
        }

        @Override
        <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                                                                                                 Object channel,
                                                                                                 Graph<NetNode<PT, NDT, NetFlowSharedEdge>, NetFlowSharedEdge> graph,
                                                                                                 long amount,
                                                                                                 long queryTick) {
            if (amount == 0) return;
            recalculateFlowLimits(queryTick);

            // check against reverse edge
            NetFlowSharedEdge inverse = graph.getEdge(getCastTarget(), getCastSource());
            if (inverse != null && inverse != NetFlowSharedEdge.this) {
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
                if (v == null) v = 0L;
                v += finalAmount;
                if (v <= 0) return null;
                return v;
            });
            sharedCapacity -= finalAmount;
            boundCapacity();
        }

        @Override
        public void recalculateFlowLimits(long queryTick) {
            if (!this.init) {
                this.maxCapacity = (long) getMinData().getThroughput() * flowBufferTicks;
                this.init = true;
            }
            int time = (int) (queryTick - this.lastQueryTick);
            if (time < 0) {
                this.map.clear();
            } else {
                List<Object> toRemove = new ObjectArrayList<>();
                long regenerationPer = MathHelper.ceil((double) time * getMinData().getThroughput() / map.size());
                map.replaceAll((k, v) -> {
                    v -= regenerationPer;
                    if (v <= 0) toRemove.add(k);
                    return v;
                });
                sharedCapacity += regenerationPer * map.size();
                boundCapacity();
                toRemove.forEach(map::removeLong);
            }
            this.lastQueryTick = queryTick;
        }

        private void boundCapacity() {
            if (this.sharedCapacity > this.maxCapacity) this.sharedCapacity = this.maxCapacity;
            else if (this.sharedCapacity < 0) this.sharedCapacity = 0;
        }
    }
}
