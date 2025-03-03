package gregtech.api.configurator.playerdata;

import gregtech.api.configurator.profile.ConfiguratorProfileRegistry;
import gregtech.api.configurator.profile.IMachineConfiguratorProfile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class PlayerConfiguratorData implements INBTSerializable<NBTTagCompound> {

    public static final String NO_PROFILE_NBT_KEY = "NO_PROFILE_SET";
    private static final String PROFILE_NBT_KEY = "Profile";
    private static final String CONFIG_NBT_KEY = "Config";

    private final Map<String, NBTTagCompound> SLOT_MAP = new Object2ObjectOpenHashMap<>();

    public PlayerConfiguratorData(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

    public PlayerConfiguratorData() {}

    public boolean hasSlot(String name) {
        return SLOT_MAP.containsKey(name);
    }

    @NotNull
    private NBTTagCompound getSlot(String name) {
        return SLOT_MAP.computeIfAbsent(name, ignored -> {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setString(PROFILE_NBT_KEY, NO_PROFILE_NBT_KEY);
            tag.setTag(CONFIG_NBT_KEY, new NBTTagCompound());

            return tag;
        });
    }

    @Nullable
    public IMachineConfiguratorProfile getSlotProfile(String name) {
        return ConfiguratorProfileRegistry.getMachineConfiguratorProfile(getSlot(name).getString(PROFILE_NBT_KEY));
    }

    public void setSlotProfile(String name, IMachineConfiguratorProfile profile) {
        getSlot(name).setString(PROFILE_NBT_KEY, profile.getName());
    }

    @NotNull
    public NBTTagCompound getSlotConfig(String name) {
        return getSlot(name).getCompoundTag(CONFIG_NBT_KEY);
    }

    public void setSlotConfig(String name, @NotNull NBTTagCompound tag) {
        SLOT_MAP.get(name).setTag(CONFIG_NBT_KEY, tag);
    }

    public Set<String> getSlots() {
        return SLOT_MAP.keySet();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        SLOT_MAP.forEach(tag::setTag);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (String name : nbt.getKeySet()) {
            SLOT_MAP.put(name, nbt.getCompoundTag(name));
        }
    }
}
