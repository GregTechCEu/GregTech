package gtqt.api.util.wireless;

import gregtech.api.metatileentity.MetaTileEntity;

import gregtech.api.util.GTUtility;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityWirelessController;

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
    private Map<UUID, NetworkNode> networks = new HashMap<>();

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
                    //nodeTag.getInteger("id"),
                    nodeTag.getString("name")
            );
            node.setEnergy(new BigInteger(nodeTag.getString("energy")));
            node.setOpen(nodeTag.getBoolean("isOpen"));
            NBTTagList listMtes = nodeTag.getTagList("Mtes", 10);
            node.hatches.clear(); // 清空原有数据
            //*****************************//
            for (int i = 0; i < listMtes.tagCount(); i++) {
                NBTTagCompound entry = listMtes.getCompoundTagAt(i);
                int dim = entry.getInteger("dim");
                BlockPos pos = new BlockPos(
                        entry.getInteger("x"),
                        entry.getInteger("y"),
                        entry.getInteger("z")
                );
                World world = NetworkManager.getWorldByDimension(dim);
                if (world != null) {
                    MetaTileEntity mte = GTUtility.getMetaTileEntity(world,pos);
                    if (mte != null) {
                        node.hatches.add((MetaTileEntityWirelessController)mte);
                    }
                }
            }
            //*****************************//
            networks.put(node.getOwnerUUID(), node);
        }
    }

    // 保存数据到NBT
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (NetworkNode node : networks.values()) {
            NBTTagCompound nodeTag = new NBTTagCompound();
            nodeTag.setString("owner", node.getOwnerUUID().toString());
            //nodeTag.setInteger("id", node.getNetworkID());
            nodeTag.setString("name", node.getNetworkName());
            nodeTag.setString("energy", node.getEnergy().toString());
            nodeTag.setBoolean("isOpen", node.isOpen());
            //*****************************//
            NBTTagList listMte = new NBTTagList();
            for (var i : node.hatches) {
                if (i.getWorld() == null) continue; // 跳过无效的 world
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("dim", i.getWorld().provider.getDimension());
                entry.setInteger("x", i.getPos().getX());
                entry.setInteger("y", i.getPos().getY());
                entry.setInteger("z", i.getPos().getZ());
                listMte.appendTag(entry);
            }
            nodeTag.setTag("Mtes", listMte);
            //*****************************//
            list.appendTag(nodeTag);
        }
        nbt.setTag("networks", list);
        return nbt;
    }
    // 新增Getter方法（线程安全）
    public Map<UUID, NetworkNode> getNetworks() {
        return Collections.unmodifiableMap(networks); // 返回不可修改的视图
    }

    // 新增直接操作方法（替代直接访问Map）
    public void addNetwork(NetworkNode node) {
        networks.put(node.getOwnerUUID(), node);
        markDirty();
    }

    public NetworkNode getNetwork(UUID id) {
        UUID find=null;
        var list = NetworkManager.getPartList(id);
        for (var x : list)
        {
            if(networks.containsKey(x))
            {
                find = x;
                break;
            }
        }
        if(find==null)
            return null;
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
