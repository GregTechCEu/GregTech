package gregtech.api.graphnet.net;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphClassType;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

public class BlockPosNode extends NetNode {

    public static final GraphClassType<BlockPosNode> TYPE = new GraphClassType<>(GTValues.MODID, "BlockPosNode",
            BlockPosNode::new);

    private BlockPos pos;

    public BlockPosNode(IGraphNet net) {
        super(net);
    }

    public BlockPosNode setPos(BlockPos pos) {
        this.pos = pos;
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

    @Override
    public @NotNull GraphClassType<? extends BlockPosNode> getType() {
        return TYPE;
    }
}
