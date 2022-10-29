package gregtech.api.net;

import gregtech.api.GTValues;
import gregtech.api.net.packets.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static gregtech.api.net.PacketHandler.*;

public class NetworkHandler {

    public static FMLEventChannel channel;

    private NetworkHandler() {
    }

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
        registerPacket(CPacketClipboardNBTUpdate.class);

        initServer();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            initClient();
        }
    }

    // Register packets as "received on server" here
    protected static void initServer() {
        registerServerExecutor(CPacketUIClientAction.class);
        registerServerExecutor(CPacketClipboardUIWidgetUpdate.class);
        registerServerExecutor(CPacketPluginSynced.class);
        registerServerExecutor(CPacketRecoverMTE.class);
        registerServerExecutor(CPacketKeysPressed.class);
        registerServerExecutor(CPacketClipboardNBTUpdate.class);
    }

    // Register packets as "received on client" here
    @SideOnly(Side.CLIENT)
    protected static void initClient() {
        registerClientExecutor(SPacketUIOpen.class);
        registerClientExecutor(SPacketUIWidgetUpdate.class);
        registerClientExecutor(SPacketBlockParticle.class);
        registerClientExecutor(SPacketClipboard.class);
        registerClientExecutor(CPacketFluidVeinList.class);
        registerClientExecutor(SPacketNotifyCapeChange.class);
        registerClientExecutor(SPacketReloadShaders.class);
    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) throws Exception {
        IPacket packet = NetworkUtils.proxy2packet(event.getPacket());
        if (hasClientExecutor(packet.getClass())) {
            NetHandlerPlayClient handler = (NetHandlerPlayClient) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                packet.executeClient(handler);
            } else {
                threadListener.addScheduledTask(() -> packet.executeClient(handler));
            }
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) throws Exception {
        IPacket packet = NetworkUtils.proxy2packet(event.getPacket());
        if (hasServerExecutor(packet.getClass())) {
            NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
            IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
            if (threadListener.isCallingFromMinecraftThread()) {
                packet.executeServer(handler);
            } else {
                threadListener.addScheduledTask(() -> packet.executeServer(handler));
            }
        }
    }
}
