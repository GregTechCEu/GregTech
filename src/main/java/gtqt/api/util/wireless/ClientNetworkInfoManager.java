package gtqt.api.util.wireless;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientNetworkInfoManager {

    private final Map<UUID, NetworkInfo> networkMap = new ConcurrentHashMap<>();

    private static ClientNetworkInfoManager instance;

    public static synchronized ClientNetworkInfoManager getInstance() {
        if (instance == null) {
            instance = new ClientNetworkInfoManager();
        }
        return instance;
    }

    public synchronized void addOrUpdateNetwork(NetworkInfo info) {
        networkMap.put(info.ownerid, info);
    }

    public synchronized void removeNetwork(UUID ownerid) {
        networkMap.remove(ownerid);
    }

    public NetworkInfo getNetworkInfo(UUID ownerid) {
        return networkMap.get(ownerid);
    }

    public Collection<NetworkInfo> getAllNetworks() {
        return networkMap.values();
    }
}
