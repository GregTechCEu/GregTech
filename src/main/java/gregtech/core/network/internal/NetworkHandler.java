package gregtech.core.network.internal;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.ModuleStage;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.INetworkHandler;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.core.CoreModule;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.Unpooled;

public class NetworkHandler implements INetworkHandler {

    private static final NetworkHandler INSTANCE = new NetworkHandler();

    private final FMLEventChannel channel;
    private final PacketHandler packetHandler;

    private NetworkHandler() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(GTValues.MODID);
        channel.register(this);
        packetHandler = PacketHandler.getInstance();
    }

    public static INetworkHandler getInstance() {
        return INSTANCE;
    }

    public void registerPacket(Class<? extends IPacket> packetClass) {
        if (GregTechAPI.moduleManager.hasPassedStage(ModuleStage.PRE_INIT)) {
            CoreModule.logger.error("Could not register packet {}, as packet registration has ended!",
                    packetClass.getName());
            return;
        }

        boolean hasServerExecutor = IServerExecutor.class.isAssignableFrom(packetClass);
        boolean hasClientExecutor = IClientExecutor.class.isAssignableFrom(packetClass);

        if (hasServerExecutor && hasClientExecutor) {
            CoreModule.logger.error(
                    "Could not register packet {}, as it is both a Server and Client executor! Only one allowed. Skipping...",
                    packetClass.getName());
            return;
        }
        if (!hasServerExecutor && !hasClientExecutor) {
            CoreModule.logger.error(
                    "Could not register packet {}, as it does not have an executor! Must have either IServerExecutor OR IClientExecutor. Skipping...",
                    packetClass.getName());
            return;
        }
        packetHandler.registerPacket(packetClass);
    }

    @Override
    public void sendToAll(IPacket packet) {
        channel.sendToAll(toFMLPacket(packet));
    }

    @Override
    public void sendTo(IPacket packet, EntityPlayerMP player) {
        channel.sendTo(toFMLPacket(packet), player);
    }

    @Override
    public void sendToAllAround(IPacket packet, TargetPoint point) {
        channel.sendToAllAround(toFMLPacket(packet), point);
    }

    @Override
    public void sendToAllTracking(IPacket packet, TargetPoint point) {
        channel.sendToAllTracking(toFMLPacket(packet), point);
    }

    @Override
    public void sendToAllTracking(IPacket packet, Entity entity) {
        channel.sendToAllTracking(toFMLPacket(packet), entity);
    }

    @Override
    public void sendToDimension(IPacket packet, int dimensionId) {
        channel.sendToDimension(toFMLPacket(packet), dimensionId);
    }

    @Override
    public void sendToServer(IPacket packet) {
        channel.sendToServer(toFMLPacket(packet));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) throws Exception {
        IPacket packet = toGTPacket(event.getPacket());
        if (IClientExecutor.class.isAssignableFrom(packet.getClass())) {
            IClientExecutor executor = (IClientExecutor) packet;
            NetHandlerPlayClient handler = (NetHandlerPlayClient) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                executor.executeClient(handler);
            } else {
                threadListener.addScheduledTask(() -> executor.executeClient(handler));
            }
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) throws Exception {
        IPacket packet = toGTPacket(event.getPacket());
        if (IServerExecutor.class.isAssignableFrom(packet.getClass())) {
            IServerExecutor executor = (IServerExecutor) packet;
            NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                executor.executeServer(handler);
            } else {
                threadListener.addScheduledTask(() -> executor.executeServer(handler));
            }
        }
    }

    private FMLProxyPacket toFMLPacket(IPacket packet) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(packetHandler.getPacketId(packet.getClass()));
        packet.encode(buf);
        return new FMLProxyPacket(buf, GTValues.MODID);
    }

    private IPacket toGTPacket(FMLProxyPacket proxyPacket) throws Exception {
        PacketBuffer payload = (PacketBuffer) proxyPacket.payload();
        IPacket packet = packetHandler.getPacketClass(payload.readVarInt()).newInstance();
        packet.decode(payload);
        return packet;
    }
}
