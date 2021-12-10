package gregtech.api.net.packets;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.net.IPacket;
import gregtech.api.net.NetworkUtils;
import lombok.NoArgsConstructor;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

@NoArgsConstructor
public class PacketRecoverMTE implements IPacket {

    private int dimension;
    private BlockPos pos;

    public PacketRecoverMTE(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(dimension);
        buf.writeBlockPos(pos);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.dimension = buf.readVarInt();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        TileEntity te = NetworkUtils.getTileEntityServer(dimension, pos);
        if (te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).isValid()) {
            ((MetaTileEntityHolder) te).sendInitialSyncData();
        }
    }
}
