package gregtech.api.graphnet.servernet;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.util.DimensionPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ServerNetNode extends NetNode {

    private DimensionPos pos;

    public ServerNetNode(IGraphNet net) {
        super(net);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setLong("Pos", pos.getPos().toLong());
        tag.setInteger("Dim", pos.getDimension());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        BlockPos pos = BlockPos.fromLong(nbt.getLong("Pos"));
        this.pos = new DimensionPos(pos, nbt.getInteger("Dim"));
    }

    @Override
    public DimensionPos getEquivalencyData() {
        return pos;
    }
}
