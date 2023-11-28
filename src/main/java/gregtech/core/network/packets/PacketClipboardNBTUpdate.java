package gregtech.core.network.packets;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import gregtech.core.network.NetworkUtils;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketClipboardNBTUpdate implements IPacket, IServerExecutor {

    private int dimension;
    private BlockPos pos;
    private int id;
    private PacketBuffer updateData;

    @SuppressWarnings("unused")
    public PacketClipboardNBTUpdate() {}

    public PacketClipboardNBTUpdate(int dimension, BlockPos pos, int id, PacketBuffer updateData) {
        this.dimension = dimension;
        this.pos = pos;
        this.id = id;
        this.updateData = updateData;
    }

    @Override
    public void encode(PacketBuffer buf) {
        NetworkUtils.writePacketBuffer(buf, updateData);
        buf.writeVarInt(dimension);
        buf.writeBlockPos(pos);
        buf.writeVarInt(id);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.updateData = NetworkUtils.readPacketBuffer(buf);
        this.dimension = buf.readVarInt();
        this.pos = buf.readBlockPos();
        this.id = buf.readVarInt();
    }

    // TODO This could still be cleaned up
    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        TileEntity te = NetworkUtils.getTileEntityServer(dimension, pos);
        if (te instanceof IGregTechTileEntity &&
                ((IGregTechTileEntity) te).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
            try {
                ((MetaTileEntityClipboard) ((IGregTechTileEntity) te).getMetaTileEntity())
                        .setClipboardNBT(updateData.readCompoundTag());
            } catch (Exception ignored) {}
        }
    }
}
