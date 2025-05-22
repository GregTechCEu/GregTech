package gtqt.api.util.wireless;

import gtqt.api.util.wireless.packages.WirelessNetworkHandler;
import gtqt.api.util.wireless.packages.PacketRequestRemoveNetwork;
import gtqt.api.util.wireless.packages.PacketSyncNetworkInfo;

import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkInfoManager {

    private final NetworkInfoStorage storage;
    private final Map<UUID, NetworkInfo> networkMap = new ConcurrentHashMap<>();

    private NetworkInfoManager(World world) {
        this.storage = NetworkInfoStorage.get(world);
        this.networkMap.putAll(storage.getAllNetworkInfo());
    }

    private static NetworkInfoManager instance;

    public static synchronized NetworkInfoManager get(World world) {
        if (instance == null) {
            instance = new NetworkInfoManager(world);
        }
        return instance;
    }

    public synchronized void addOrUpdateNetwork(NetworkInfo info) {
        if (info.ownerid == null) {
            throw new IllegalArgumentException("NetworkInfo ownerid cannot be null");
        }
        networkMap.put(info.ownerid, info);
        storage.addNetworkInfo(info);
        storage.markDirty();
        WirelessNetworkHandler.sendToAll(new PacketSyncNetworkInfo(info));
    }

    public NetworkInfo getNetworkInfo(UUID ownerid) {
        return networkMap.get(ownerid);
    }

    public synchronized void removeNetwork(UUID ownerid) {
        if (networkMap.containsKey(ownerid)) {
            networkMap.remove(ownerid);
            storage.removeNetworkInfo(ownerid);
            storage.markDirty();
            WirelessNetworkHandler.sendToAll(new PacketRequestRemoveNetwork(ownerid,ownerid));
        }
    }

    public Collection<NetworkInfo> getAllNetworks() {
        return networkMap.values();
    }

}
