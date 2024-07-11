package gregtech.api.graphnet.worldnet;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;

import gregtech.api.graphnet.pipenet.WorldPipeNet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class WorldNetNode extends NetNode {

    private BlockPos pos;

    public WorldNetNode(WorldNet net) {
        super(net);
    }

    @Override
    public WorldNet getNet() {
        return (WorldNet) super.getNet();
    }

    public WorldNetNode setPos(BlockPos pos) {
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
    public Object getEquivalencyData() {
        return pos;
    }
}
