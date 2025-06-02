package gtqt.api.util.wireless;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkDatabase extends WorldSavedData {
    private static final String DATA_NAME = "gtqt_network_data";
    private Map<Integer, NetworkNode> networks = new HashMap<>();

    public NetworkDatabase() {
        super(DATA_NAME);
    }
    public NetworkDatabase(String name) {
        super(name);
    }
    // 从NBT加载数据
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("networks", 10);
        for (NBTBase tag : list) {
            NBTTagCompound nodeTag = (NBTTagCompound) tag;
            NetworkNode node = new NetworkNode(
                    UUID.fromString(nodeTag.getString("owner")),
                    nodeTag.getInteger("id"),
                    nodeTag.getString("name")
            );
            node.setEnergy(new BigInteger(nodeTag.getString("energy")));
            node.setOpen(nodeTag.getBoolean("isOpen"));

            NBTTagList machineList = nbt.getTagList("machines", 10);
            for (NBTBase tagbase : machineList) {
                node.addMachine(WorldBlockPos.fromNBT((NBTTagCompound) tagbase));
            }
            networks.put(node.getNetworkID(), node);
        }
    }

    // 保存数据到NBT
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (NetworkNode node : networks.values()) {
            NBTTagCompound nodeTag = new NBTTagCompound();
            nodeTag.setString("owner", node.getOwnerUUID().toString());
            nodeTag.setInteger("id", node.getNetworkID());
            nodeTag.setString("name", node.getNetworkName());
            nodeTag.setString("energy", node.getEnergy().toString());
            nodeTag.setBoolean("isOpen", node.isOpen());

            NBTTagList machineList = new NBTTagList();
            for (WorldBlockPos wpos : node.machines) {
                machineList.appendTag(wpos.toNBT());
            }
            nbt.setTag("machines", machineList);
            list.appendTag(nodeTag);
        }
        nbt.setTag("networks", list);
        return nbt;
    }
    // 新增Getter方法（线程安全）
    public Map<Integer, NetworkNode> getNetworks() {
        return Collections.unmodifiableMap(networks); // 返回不可修改的视图
    }

    // 新增直接操作方法（替代直接访问Map）
    public void addNetwork(NetworkNode node) {
        networks.put(node.getNetworkID(), node);
        markDirty();
    }

    public NetworkNode getNetwork(int id) {
        return networks.get(id);
    }

    public static NetworkDatabase get(World world) {
        MapStorage storage = world.getMapStorage();
        NetworkDatabase instance = (NetworkDatabase) storage.getOrLoadData(NetworkDatabase.class, DATA_NAME);
        if (instance == null) {
            instance = new NetworkDatabase();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
}
