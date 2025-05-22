package gtqt.api.util.wireless;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkInfoStorage extends WorldSavedData {
    private static final String DATA_NAME = "gregtech_gtqt_network";
    private Map<UUID, NetworkInfo> networkMap = new HashMap<>();

    public NetworkInfoStorage() {
        super(DATA_NAME);
    }

    public NetworkInfoStorage(String name) {
        super(name);
    }

    public Map<UUID, NetworkInfo> getAllNetworkInfo() {
        return new HashMap<>(networkMap);
    }

    public static NetworkInfoStorage get(World world) {
        if (world.provider.getDimension() != 0) {
            world = world.getMinecraftServer().getWorld(0);
        }
        MapStorage storage = world.getMapStorage();
        NetworkInfoStorage instance = (NetworkInfoStorage) storage.getOrLoadData(NetworkInfoStorage.class, DATA_NAME);
        if (instance == null) {
            instance = new NetworkInfoStorage();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }

    public void addNetworkInfo(NetworkInfo info) {
        if (info.ownerid != null) {
            networkMap.put(info.ownerid, info);
            this.markDirty();
        }
    }

    public NetworkInfo getNetworkInfo(UUID ownerid) {
        return networkMap.get(ownerid);
    }

    public void removeNetworkInfo(UUID ownerid) {
        if (networkMap.containsKey(ownerid)) {
            networkMap.remove(ownerid);
            this.markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        networkMap.clear();
        NBTTagList list = nbt.getTagList("networks", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            NetworkInfo info = new NetworkInfo();
            info.readFromNBT(tag);
            if (info.ownerid != null) {
                networkMap.put(info.ownerid, info);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (NetworkInfo info : networkMap.values()) {
            list.appendTag(info.writeToNBT());
        }
        compound.setTag("networks", list);
        return compound;
    }
}
