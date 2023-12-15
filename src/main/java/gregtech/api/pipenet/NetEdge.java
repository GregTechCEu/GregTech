package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.util.function.TriConsumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;

class NetEdge extends DefaultWeightedEdge implements INBTSerializable<NBTTagCompound> {

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
        return null;
    }

    /**
     * Use {@link Builder} instead, this does nothing.
     */
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}

    static final class Builder<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType extends INodeData> {
        private final NodeG<PipeType, NodeDataType> node1;
        private final NodeG<PipeType, NodeDataType> node2;
        private final double weight;
        private final boolean buildable;

        private final TriConsumer<NodeG<PipeType, NodeDataType>, NodeG<PipeType, NodeDataType>, Double> edgeProducer;
         Builder(Map<Long, NodeG<PipeType, NodeDataType>> longPosMap, NBTTagCompound tag, TriConsumer<NodeG<PipeType, NodeDataType>, NodeG<PipeType, NodeDataType>, Double> edgeProducer) {
             this.node1 = longPosMap.get(tag.getLong("SourceLongPos"));
             this.node2 = longPosMap.get(tag.getLong("TargetLongPos"));
             this.weight = tag.getDouble("Weight");
             this.edgeProducer = edgeProducer;
             this.buildable = node1 != null && node2 != null;
         }

         void addIfBuildable() {
             if (buildable) {
                 edgeProducer.accept(node1, node2, weight);
             }
         }
    }
}
