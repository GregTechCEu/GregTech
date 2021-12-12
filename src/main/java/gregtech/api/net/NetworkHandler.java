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

        registerPacket(PacketUIOpen.class);
        registerPacket(PacketUIWidgetUpdate.class);
        registerPacket(PacketUIClientAction.class);
        registerPacket(PacketBlockParticle.class);
        registerPacket(PacketClipboard.class);
        registerPacket(PacketClipboardUIWidgetUpdate.class);
        registerPacket(PacketPluginSynced.class);
        registerPacket(PacketRecoverMTE.class);
        registerPacket(PacketKeysPressed.class);
        registerPacket(PacketFluidVeinList.class);

        initServer();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            initClient();
        }
    }

    // Register packets as "received on server" here
    protected static void initServer() {
        registerServerExecutor(PacketUIClientAction.class);
        registerServerExecutor(PacketClipboardUIWidgetUpdate.class);
        registerServerExecutor(PacketPluginSynced.class);
        registerServerExecutor(PacketRecoverMTE.class);
        registerServerExecutor(PacketKeysPressed.class);
    }

    // Register packets as "received on client" here
    @SideOnly(Side.CLIENT)
    protected static void initClient() {
        registerClientExecutor(PacketUIOpen.class);
        registerClientExecutor(PacketUIWidgetUpdate.class);
        registerClientExecutor(PacketBlockParticle.class);
        registerClientExecutor(PacketClipboard.class);
        registerClientExecutor(PacketFluidVeinList.class);
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
