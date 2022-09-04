package gregtech.api.net;

import gregtech.api.util.GTLog;
import net.minecraft.util.IntIdentityHashBiMap;

public class PacketHandler {

    private static final PacketHandler INSTANCE = new PacketHandler(10);

    private final IntIdentityHashBiMap<Class<? extends IPacket>> packetMap;

    private PacketHandler(int initialCapacity) {
        packetMap = new IntIdentityHashBiMap<>(initialCapacity);
    }

    private static int ID = 1;
    public static void registerPacket(Class<? extends IPacket> packetClass) {
        INSTANCE.packetMap.put(packetClass, ID++);

        // Ensure packet is not marked as both a client and server executor
        if (IServerExecutor.class.isAssignableFrom(packetClass) && IClientExecutor.class.isAssignableFrom(packetClass)) {
            GTLog.logger.error("Packet {} cannot be marked as server and client executor, skipping...", packetClass.toGenericString());
        }
    }

    public static int getPacketId(Class<? extends IPacket> packetClass) {
        return INSTANCE.packetMap.getId(packetClass);
    }

    public static Class<? extends IPacket> getPacketClass(int packetId) {
        return INSTANCE.packetMap.get(packetId);
    }
}
