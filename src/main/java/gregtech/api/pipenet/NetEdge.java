package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.util.function.QuadConsumer;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public final class NetEdge extends DefaultWeightedEdge implements INBTSerializable<NBTTagCompound> {

    private AbstractEdgePredicate<?> predicate;
    private boolean invertedPredicate;

    @SuppressWarnings("unused") // used via reflection
    public NetEdge() {}

    public void setPredicate(AbstractEdgePredicate<?> predicate) {
        this.predicate = predicate;
        this.invertedPredicate = predicate.sourcePos != this.getSource().getNodePos();
    }

    public Predicate<Object> getPredicate() {
        // if we don't have a predicate, just assume that we're good.
        if (predicate == null) return (a) -> true;
        return predicate;
    }

    public boolean isPredicateInverted() {
        return invertedPredicate;
    }

    @Override
    public NodeG<?, ?> getSource() {
        return (NodeG<?, ?>) super.getSource();
    }

    @Override
    public NodeG<?, ?> getTarget() {
        return (NodeG<?, ?>) super.getTarget();
    }

    @SuppressWarnings("unchecked")
    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> NodeG<PT, NDT> getCastSource() {
        return (NodeG<PT, NDT>) getSource();
    }

    @SuppressWarnings("unchecked")
    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> NodeG<PT, NDT> getCastTarget() {
        return (NodeG<PT, NDT>) getTarget();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("SourceLongPos", getSource().getLongPos());
        tag.setLong("TargetLongPos", getTarget().getLongPos());
        tag.setDouble("Weight", getWeight());
        if (predicate != null) tag.setTag("Predicate", AbstractEdgePredicate.toNBT(predicate));
        tag.setBoolean("InvertedPredicate", isPredicateInverted());
        return tag;
    }

    /**
     * Use {@link NBTBuilder} instead, this does nothing.
     */
    @Override
    @Deprecated
    public void deserializeNBT(NBTTagCompound nbt) {}

    static final class NBTBuilder<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>> implements INBTBuilder {

        private final NodeG<PipeType, NodeDataType> node1;
        private final NodeG<PipeType, NodeDataType> node2;
        private final AbstractEdgePredicate<?> predicate;
        private final double weight;
        private final boolean buildable;

        private final QuadConsumer<NodeG<PipeType, NodeDataType>, NodeG<PipeType, NodeDataType>, Double, AbstractEdgePredicate<?>> edgeProducer;

        NBTBuilder(Map<Long, NodeG<PipeType, NodeDataType>> longPosMap, NBTTagCompound tag,
                   QuadConsumer<NodeG<PipeType, NodeDataType>, NodeG<PipeType, NodeDataType>, Double, AbstractEdgePredicate<?>> edgeProducer) {
            this.node1 = longPosMap.get(tag.getLong("SourceLongPos"));
            this.node2 = longPosMap.get(tag.getLong("TargetLongPos"));
            this.weight = tag.getDouble("Weight");
            this.predicate = AbstractEdgePredicate.nbtPredicate(tag.getCompoundTag("Predicate"));
            if (predicate != null) {
                if (tag.getBoolean("InvertedPredicate"))
                    this.predicate.setPosInfo(this.node2.getNodePos(), this.node1.getNodePos());
                else this.predicate.setPosInfo(this.node1.getNodePos(), this.node2.getNodePos());
            }
            this.edgeProducer = edgeProducer;
            this.buildable = node1 != null && node2 != null;
        }

        @Override
        public void build() {
            if (buildable) {
                edgeProducer.accept(node1, node2, weight, predicate);
            }
        }
    }

    /// Flow behavior related code ///

    private int flowBufferTicks;
    private ChannelsHolder channels;
    private WeakHashMap<ChannelSimulatorKey, ChannelsHolder> simulatedChannels;

    private INodeData<? extends INodeData<?>> minData;

    public NetEdge(int flowBufferTicks) {
        this.flowBufferTicks = flowBufferTicks;
        this.channels = new ChannelsHolder();
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

    private ChannelsHolder getChannels(@Nullable NetEdge.ChannelSimulatorKey simulator) {
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
        if (this.minData == null) this.minData = this.getCastSource().getData().getMinData(this.getCastTarget().getData());
        return this.minData;
    }

    private int getAdjustedThroughput() {
        return getMinData().getThroughput() * flowBufferTicks;
    }

    private boolean cannotSupportChannel(Object channel, long queryTick, @Nullable NetEdge.ChannelSimulatorKey simulator) {
        var channels = getChannels(simulator);
        channels.recalculateFlowLimits(queryTick);
        return channels.map.size() >= getMinData().getChannelMaxCount() && !channels.map.containsKey(channel);
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getFlowLimit(
            Object channel, Graph<NodeG<PT, NDT>, NetEdge> graph, long queryTick, @Nullable NetEdge.ChannelSimulatorKey simulator) {
        if (this.cannotSupportChannel(channel, queryTick, simulator)) {
            return 0;
        }
        int limit = getChannels(simulator).map.getOrDefault(channel, getAdjustedThroughput());

        NetEdge inverse = graph.getEdge(this.getCastTarget(), this.getCastSource());
        if (inverse != null && inverse != this) {
            if (inverse.cannotSupportChannel(channel, queryTick, simulator)) return 0;
            limit += inverse.getConsumedLimit(channel, queryTick, simulator);
        }

        return limit;
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> int getConsumedLimit(
            Object channel, long queryTick, @Nullable NetEdge.ChannelSimulatorKey simulator) {
        var channels = getChannels(simulator);
        channels.recalculateFlowLimits(queryTick);
        int limit = getAdjustedThroughput();
        return limit - channels.map.getOrDefault(channel, limit);
    }

    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
            Object channel, Graph<NodeG<PT, NDT>, NetEdge> graph, int amount, long queryTick, @Nullable NetEdge.ChannelSimulatorKey simulator) {
        if (amount == 0) return;
        var channels = getChannels(simulator);
        channels.recalculateFlowLimits(queryTick);

        // check against reverse edge
        NetEdge inverse = graph.getEdge(this.getCastTarget(), this.getCastSource());
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
