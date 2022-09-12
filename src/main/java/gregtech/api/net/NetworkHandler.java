package gregtech.api.net;

import gregtech.GregTechMod;
import gregtech.api.GTValues;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
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

    public static void init(ASMDataTable table) {
        // don't call this method pls :pleading:
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null || container.getMod() != GregTechMod.instance || channel != null) {
            return;
        }

        // register the channel
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(GTValues.MODID);
        channel.register(new NetworkHandler());

        // register the packets
        //noinspection unchecked
        NetworkUtils.getPacketClasses(table).forEach(p -> registerPacket((Class<? extends IPacket>) p));
    }

    private static int ID = 1;
    private static void registerPacket(Class<? extends IPacket> packetClass) {
        packetMap.put(packetClass, ID++);
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
