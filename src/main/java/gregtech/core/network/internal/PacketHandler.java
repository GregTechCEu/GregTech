package gregtech.core.network.internal;

import gregtech.api.network.IPacket;

import net.minecraft.util.IntIdentityHashBiMap;

public class PacketHandler {

    private static final PacketHandler INSTANCE = new PacketHandler(10);

    private final IntIdentityHashBiMap<Class<? extends IPacket>> packetMap;

    private PacketHandler(int initialCapacity) {
        packetMap = new IntIdentityHashBiMap<>(initialCapacity);
    }

    protected static PacketHandler getInstance() {
        return INSTANCE;
    }

    private int ID = 1;

    public void registerPacket(Class<? extends IPacket> packetClass) {
        packetMap.put(packetClass, ID++);
    }

    public int getPacketId(Class<? extends IPacket> packetClass) {
        return packetMap.getId(packetClass);
    }

    public Class<? extends IPacket> getPacketClass(int packetId) {
        return packetMap.get(packetId);
    }
}
