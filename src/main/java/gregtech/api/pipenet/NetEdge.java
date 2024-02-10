package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.util.function.QuadConsumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;
import java.util.function.Predicate;

public final class NetEdge extends DefaultWeightedEdge implements INBTSerializable<NBTTagCompound> {

    private AbstractEdgePredicate<?> predicate;
    private boolean invertedPredicate;

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
}
