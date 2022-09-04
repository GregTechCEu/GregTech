package gregtech.api.net;

import gregtech.api.GTValues;
import gregtech.api.net.packets.*;
import gregtech.api.util.GTLog;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetworkHandler {

    public static FMLEventChannel channel;

    private NetworkHandler() {
    }

    protected static final IntIdentityHashBiMap<Class<? extends IPacket>> packetMap = new IntIdentityHashBiMap<>(15);

    // Register your packets here
    public static void init() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(GTValues.MODID);
        channel.register(new NetworkHandler());

        registerPacket(SPacketUIOpen.class);
        registerPacket(SPacketUIWidgetUpdate.class);
        registerPacket(CPacketUIClientAction.class);
        registerPacket(SPacketBlockParticle.class);
        registerPacket(SPacketClipboard.class);
        registerPacket(CPacketClipboardUIWidgetUpdate.class);
        registerPacket(CPacketPluginSynced.class);
        registerPacket(CPacketRecoverMTE.class);
        registerPacket(CPacketKeysPressed.class);
        registerPacket(CPacketFluidVeinList.class);
        registerPacket(SPacketNotifyCapeChange.class);
        registerPacket(SPacketReloadShaders.class);
    }

    private static int ID = 1;
    private static void registerPacket(Class<? extends IPacket> packetClass) {
        packetMap.put(packetClass, ID++);

        // Ensure packet is not marked as both a client and server executor
        if (IServerExecutor.class.isAssignableFrom(packetClass) && IClientExecutor.class.isAssignableFrom(packetClass)) {
            GTLog.logger.error("Packet {} cannot be marked as server and client executor, skipping...", packetClass.toGenericString());
        }
    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) throws Exception {
        IPacket packet = NetworkUtils.proxy2packet(event.getPacket());
        if (IClientExecutor.class.isAssignableFrom(packet.getClass())) {
            IClientExecutor clientExecutor = (IClientExecutor) packet;
            NetHandlerPlayClient handler = (NetHandlerPlayClient) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                clientExecutor.executeClient(handler);
            } else {
                threadListener.addScheduledTask(() -> clientExecutor.executeClient(handler));
            }
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) throws Exception {
        IPacket packet = NetworkUtils.proxy2packet(event.getPacket());
        if (IServerExecutor.class.isAssignableFrom(packet.getClass())) {
            IServerExecutor serverExecutor = (IServerExecutor) packet;
            NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                serverExecutor.executeServer(handler);
            } else {
                threadListener.addScheduledTask(() -> serverExecutor.executeServer(handler));
            }
        }
    }
}
