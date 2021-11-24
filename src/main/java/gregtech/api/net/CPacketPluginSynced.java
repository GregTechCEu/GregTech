package gregtech.api.net;

import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.function.Consumer;

public class CPacketPluginSynced implements NetworkHandler.Packet {
    public MonitorPluginBaseBehavior plugin;
    public int id;
    public Consumer<PacketBuffer> payloadWriter;
    public PacketBuffer buf;


    public CPacketPluginSynced(MonitorPluginBaseBehavior plugin, int id, Consumer<PacketBuffer> payloadWriter) {
        this.plugin = plugin;
        this.id = id;
        this.payloadWriter = payloadWriter;
    }

    CPacketPluginSynced(MonitorPluginBaseBehavior plugin, PacketBuffer buf) {
        this(plugin, buf.readVarInt(), null);
        this.buf = buf;
    }

    public static void registerPacket(int packetId) {
        NetworkHandler.registerPacket(packetId, CPacketPluginSynced.class, new NetworkHandler.PacketCodec<>(
                (packet, buf) -> {
                    MetaTileEntityMonitorScreen screen = packet.plugin.getScreen();
                    buf.writeVarInt(screen.getWorld().provider.getDimension());
                    buf.writeBlockPos(screen.getPos());
                    buf.writeVarInt(packet.id);
                    if(packet.payloadWriter != null) {
                        packet.payloadWriter.accept(buf);
                    }
                },
                (buf) -> {
                    int dim = buf.readVarInt();
                    BlockPos pos = buf.readBlockPos();
                    TileEntity te = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim).getTileEntity(pos);
                    if(te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                        MonitorPluginBaseBehavior pluginBaseBehavior = ((MetaTileEntityMonitorScreen) ((MetaTileEntityHolder) te).getMetaTileEntity()).plugin;
                        if (pluginBaseBehavior != null) {
                            return new CPacketPluginSynced(pluginBaseBehavior, buf);
                        }
                    }
                    return new CPacketPluginSynced(null, buf);
                }
        ));
    }

    public static void registerExecutor() {
        NetworkHandler.registerServerExecutor(CPacketPluginSynced.class, (packet, handler) -> {
            if (packet.plugin != null) {
                packet.plugin.readPluginAction(handler.player, packet.id, packet.buf);
            }
        });
    }

}
