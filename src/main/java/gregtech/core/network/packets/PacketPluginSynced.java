package gregtech.core.network.packets;

import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.core.network.NetworkUtils;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketPluginSynced implements IPacket, IServerExecutor {

    private int dimension;
    private BlockPos pos;
    private int id;
    private PacketBuffer updateData;

    @SuppressWarnings("unused")
    public PacketPluginSynced() {}

    public PacketPluginSynced(int dimension, BlockPos pos, int id, PacketBuffer updateData) {
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

    // TODO This could be cleaned up still
    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        TileEntity te = NetworkUtils.getTileEntityServer(dimension, pos);
        if (te instanceof IGregTechTileEntity &&
                ((IGregTechTileEntity) te).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
            MonitorPluginBaseBehavior plugin = ((MetaTileEntityMonitorScreen) ((IGregTechTileEntity) te)
                    .getMetaTileEntity()).plugin;
            if (plugin != null) {
                plugin.readPluginAction(handler.player, id, updateData);
            }
        }
    }
}
