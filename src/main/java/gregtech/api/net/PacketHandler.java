package gregtech.api.net;

import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class PacketHandler {

    private static final PacketHandler INSTANCE = new PacketHandler(10);

    private final IntIdentityHashBiMap<Class<? extends IPacket>> packetMap;

    @SideOnly(Side.CLIENT)
    private List<Class<? extends IPacket>> clientExecutors;
    private final List<Class<? extends IPacket>> serverExecutors;

    private PacketHandler(int initialCapacity) {
        packetMap = new IntIdentityHashBiMap<>(initialCapacity);
        serverExecutors = new ArrayList<>();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            clientExecutors = new ArrayList<>();
        }
    }

    private static int ID = 1;
    public static void registerPacket(Class<? extends IPacket> packetClass) {
        INSTANCE.packetMap.put(packetClass, ID++);
    }

    public static int getPacketId(Class<? extends IPacket> packetClass) {
        return INSTANCE.packetMap.getId(packetClass);
    }

    public static Class<? extends IPacket> getPacketClass(int packetId) {
        return INSTANCE.packetMap.get(packetId);
    }

    public static void registerServerExecutor(Class<? extends IPacket> packetClass) {
        INSTANCE.serverExecutors.add(packetClass);
    }

    public static boolean hasServerExecutor(Class<? extends IPacket> packetClass) {
        return INSTANCE.serverExecutors.contains(packetClass);
    }

    @SideOnly(Side.CLIENT)
    public static void registerClientExecutor(Class<? extends IPacket> packetClass) {
        INSTANCE.clientExecutors.add(packetClass);
    }

    @SideOnly(Side.CLIENT)
    public static boolean hasClientExecutor(Class<? extends IPacket> packetClass) {
        return INSTANCE.clientExecutors.contains(packetClass);
    }
}
