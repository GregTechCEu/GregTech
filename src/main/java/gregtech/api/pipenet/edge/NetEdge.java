package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INBTBuilder;
import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.predicate.AbstractEdgePredicate;
import gregtech.api.pipenet.predicate.IPredicateTestObject;
import gregtech.api.util.function.QuadConsumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;
import java.util.function.Predicate;

public class NetEdge extends DefaultWeightedEdge implements INBTSerializable<NBTTagCompound>, IEdge<NetNode<?, ?, ?>> {

    private AbstractEdgePredicate<?> predicate;
    private boolean invertedPredicate;

    private INodeData<? extends INodeData<?>> minData;

    /**
     * Most basic NetEdge that provides predicate handling & NBT storage capability
     */
    @SuppressWarnings("unused") // used via reflection
    public NetEdge() {}

    public void setPredicate(AbstractEdgePredicate<?> predicate) {
        this.predicate = predicate;
        this.invertedPredicate = predicate.getSourcePos() != this.getSource().getNodePos();
    }

    public Predicate<IPredicateTestObject> getPredicate() {
        // if we don't have a predicate, just assume that we're good.
        if (predicate == null) return (a) -> true;
        return predicate;
    }

    public INodeData<? extends INodeData<?>> getMinData() {
        if (this.minData == null)
            this.minData = this.getCastSource().getData().getMinData(this.getCastTarget().getData());
        return this.minData;
    }

    public boolean isPredicateInverted() {
        return invertedPredicate;
    }

    @Override
    public NetNode<?, ?, ?> getSource() {
        return (NetNode<?, ?, ?>) super.getSource();
    }

    @Override
    public NetNode<?, ?, ?> getTarget() {
        return (NetNode<?, ?, ?>) super.getTarget();
    }

    @SuppressWarnings("unchecked")
    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
            E extends NetEdge> NetNode<PT, NDT, E> getCastSource() {
        return (NetNode<PT, NDT, E>) getSource();
    }

    @SuppressWarnings("unchecked")
    public <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
            E extends NetEdge> NetNode<PT, NDT, E> getCastTarget() {
        return (NetNode<PT, NDT, E>) getTarget();
    }

    @Override
    protected final double getWeight() {
        return super.getWeight();
    }

    public double getDynamicWeight(IPredicateTestObject channel, SimulatorKey simulator, long queryTick) {
        return getWeight();
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

    public static final class NBTBuilder<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>, E extends NetEdge> implements INBTBuilder {

        private final NetNode<PipeType, NodeDataType, E> node1;
        private final NetNode<PipeType, NodeDataType, E> node2;
        private final AbstractEdgePredicate<?> predicate;
        private final double weight;
        private final boolean buildable;

        private final QuadConsumer<NetNode<PipeType, NodeDataType, E>, NetNode<PipeType, NodeDataType, E>, Double, AbstractEdgePredicate<?>> edgeProducer;

        public NBTBuilder(Map<Long, NetNode<PipeType, NodeDataType, E>> longPosMap, NBTTagCompound tag,
                          QuadConsumer<NetNode<PipeType, NodeDataType, E>, NetNode<PipeType, NodeDataType, E>, Double, AbstractEdgePredicate<?>> edgeProducer) {
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
}
