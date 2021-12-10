package gregtech.api.net.packets;

import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.net.IPacket;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

@NoArgsConstructor
public class PacketPluginSynced implements IPacket {

    private int dimension;
    private BlockPos pos;
    private int id;
    private PacketBuffer updateData;

    public PacketPluginSynced(int dimension, BlockPos pos, int id, PacketBuffer updateData) {
        this.dimension = dimension;
        this.pos = pos;
        this.id = id;
        this.updateData = updateData;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(updateData.readableBytes());
        buf.writeBytes(updateData);
        buf.writeVarInt(dimension);
        buf.writeBlockPos(pos);
        buf.writeVarInt(id);
    }

    @Override
    public void decode(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        this.dimension = buf.readVarInt();
        this.pos = buf.readBlockPos();
        this.id = buf.readVarInt();
        this.updateData = new PacketBuffer(copiedDataBuffer);
    }

    // TODO This could be cleaned up still
    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        TileEntity te = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension).getTileEntity(pos);
        if (te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
            MonitorPluginBaseBehavior plugin = ((MetaTileEntityMonitorScreen) ((MetaTileEntityHolder) te).getMetaTileEntity()).plugin;
            if (plugin != null) {
                plugin.readPluginAction(handler.player, id, updateData);
            }
        }
    }
}
