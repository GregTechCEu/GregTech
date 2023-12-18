package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.util.function.QuadConsumer;
import gregtech.api.util.function.TriConsumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;
import java.util.Map;

class NetEdge extends DefaultWeightedEdge implements INBTSerializable<NBTTagCompound> {

    private AbstractEdgePredicate<?> predicate;
    private boolean invertedPredicate;

    public void setPredicate(AbstractEdgePredicate<?> predicate) {
        this.predicate = predicate;
        this.invertedPredicate = predicate.sourcePos != this.getSource().getNodePos();
    }

    public AbstractEdgePredicate<?> getPredicate() {
        return predicate;
    }

    public boolean isPredicateInverted() {
        return invertedPredicate;
    }

    @Override
    protected NodeG<?, ?> getSource() {
        return (NodeG<?, ?>) super.getSource();
    }

    @Override
    protected NodeG<?, ?> getTarget() {
        return (NodeG<?, ?>) super.getTarget();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("SourceLongPos", getSource().getLongPos());
        tag.setLong("TargetLongPos", getTarget().getLongPos());
        tag.setDouble("Weight", getWeight());
        tag.setTag("Predicate", AbstractEdgePredicate.toNBT(predicate));
        tag.setBoolean("InvertedPredicate", isPredicateInverted());
        return null;
    }

    /**
     * Use {@link Builder} instead, this does nothing.
     */
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}

    static final class Builder<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>> {

        private final NodeG<PipeType, NodeDataType> node1;
        private final NodeG<PipeType, NodeDataType> node2;
        private final AbstractEdgePredicate<?> predicate;
        private final double weight;
        private final boolean buildable;

        private final QuadConsumer<NodeG<PipeType, NodeDataType>, NodeG<PipeType, NodeDataType>, Double, AbstractEdgePredicate<?>> edgeProducer;

        Builder(Map<Long, NodeG<PipeType, NodeDataType>> longPosMap, NBTTagCompound tag,
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

        void addIfBuildable() {
            if (buildable) {
                edgeProducer.accept(node1, node2, weight, predicate);
            }
        }
    }
}
