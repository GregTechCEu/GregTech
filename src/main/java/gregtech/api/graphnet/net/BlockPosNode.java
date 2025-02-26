package gregtech.api.graphnet.net;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphClassType;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

public class BlockPosNode extends NetNode {

    public static final GraphClassType<BlockPosNode> TYPE = new GraphClassType<>(GTValues.MODID, "BlockPosNode",
            BlockPosNode::new);

    private @NotNull BlockPos pos;
    private int hash;

    public BlockPosNode(IGraphNet net) {
        super(net);
        pos = BlockPos.ORIGIN;
        hash = pos.hashCode();
    }

    public BlockPosNode setPos(BlockPos pos) {
        this.pos = pos;
        hash = pos.hashCode();
        return this;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setLong("Pos", pos.toLong());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.setPos(BlockPos.fromLong(nbt.getLong("Pos")));
    }

    @Override
    public @NotNull BlockPos getEquivalencyData() {
        return pos;
    }

    // cash the hash to improve hashmap performance
    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public @NotNull GraphClassType<? extends BlockPosNode> getType() {
        return TYPE;
    }
}
