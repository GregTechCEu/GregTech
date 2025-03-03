package gregtech.api.configurator.playerdata;

import gregtech.api.GTValues;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ConfiguratorDataRegistry extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".configurator_data";
    private static final String NBT_ID = "PlayerData";

    private static final Map<UUID, PlayerConfiguratorData> PLAYER_DATA_MAP = new Object2ObjectOpenHashMap<>();

    public ConfiguratorDataRegistry(String name) {
        super(name);
    }

    public static PlayerConfiguratorData getPlayerData(UUID player) {
        return PLAYER_DATA_MAP.computeIfAbsent(player, key -> new PlayerConfiguratorData());
    }

    public static Set<String> getSlots(UUID player) {
        return getPlayerData(player).getSlots();
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        if (nbt.hasKey(NBT_ID)) {
            NBTTagCompound dataNBT = nbt.getCompoundTag(NBT_ID);

            for (String owner : dataNBT.getKeySet()) {
                NBTTagCompound playerData = dataNBT.getCompoundTag(owner);
                PLAYER_DATA_MAP.put(UUID.fromString(owner), new PlayerConfiguratorData(playerData));
            }
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound tag) {
        NBTTagCompound dataNBT = new NBTTagCompound();
        for (UUID owner : PLAYER_DATA_MAP.keySet()) {
            NBTTagCompound playerData = PLAYER_DATA_MAP.get(owner).serializeNBT();
            dataNBT.setTag(owner.toString(), playerData);
        }

        tag.setTag(NBT_ID, dataNBT);

        return tag;
    }

    public static void clearMaps() {
        PLAYER_DATA_MAP.clear();
    }

    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();

        ConfiguratorDataRegistry registry = (ConfiguratorDataRegistry) storage
                .getOrLoadData(ConfiguratorDataRegistry.class, DATA_ID);

        if (registry == null) {
            registry = new ConfiguratorDataRegistry(DATA_ID);
            storage.setData(DATA_ID, registry);
        }
    }
}
