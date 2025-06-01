package gtqt.api.util.wireless;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    // 单例模式
    public static final NetworkManager INSTANCE = new NetworkManager();
    private final ConcurrentHashMap<Integer, Object> networkLocks = new ConcurrentHashMap<>();

    // 获取当前世界的网络数据库
    private NetworkDatabase getDatabase(World world) {
        return NetworkDatabase.get(world);
    }

    // 修改后的createNetwork方法
    public NetworkNode createNetwork(World world, UUID owner, String name) {
        NetworkDatabase db = NetworkDatabase.get(world);
        int newID = generateUniqueID(db);
        NetworkNode node = new NetworkNode(owner, newID, name);
        db.addNetwork(node); // 改用安全方法操作
        return node;
    }


    public long transferEnergy(World world, int networkID, BigInteger amount) {
        if (amount.equals(BigInteger.ZERO)) return 0l;

        Object lock = networkLocks.computeIfAbsent(networkID, k -> new Object());
        synchronized (lock) {
            NetworkDatabase db = NetworkDatabase.get(world);
            NetworkNode node = db.getNetworks().get(networkID);
            if (node == null) return  0l;

            BigInteger actual = node.modifyEnergy(amount);
            if (!actual.equals(BigInteger.ZERO)) {
                db.markDirty();
            }
            return actual.longValue();
        }
    }
    // ID生成逻辑优化
    private int generateUniqueID(NetworkDatabase db) {
        return db.getNetworks().keySet().stream()
                .mapToInt(Integer::intValue)
                .max().orElse(0) + 1;
    }
    // 在NetworkManager中添加便捷方法
    public BigInteger getEnergy(World world, int networkID) {
        NetworkNode node = NetworkDatabase.get(world).getNetwork(networkID);
        return node != null ? node.getEnergy() : BigInteger.ZERO;
    }

    public boolean hasEnoughEnergy(World world, int networkID, BigInteger amount) {
        return getEnergy(world, networkID).compareTo(amount) >= 0;
    }
    public static World getWorldByDimension(int dimension) {
        MinecraftServer server = FMLServerHandler.instance().getServer();
        if (server != null) {
            return server.getWorld(dimension);
        }
        return null;
    }
}
