package gregtech.api.net.packets;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.net.IPacket;
import gregtech.api.net.NetworkUtils;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import lombok.NoArgsConstructor;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

@NoArgsConstructor
public class CPacketClipboardUIWidgetUpdate implements IPacket {

    private int dimension;
    private BlockPos pos;
    private int id;
    private PacketBuffer updateData;

    public CPacketClipboardUIWidgetUpdate(int dimension, BlockPos pos, int id, PacketBuffer updateData) {
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
        if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
            ((MetaTileEntityClipboard) ((IGregTechTileEntity) te).getMetaTileEntity()).readUIAction(handler.player, id, updateData);
        }
    }
}
