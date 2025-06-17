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

    private @Nullable NBTTagCompound getSlot(String name) {
        return SLOT_MAP.get(name);
    }

    public void deleteSlot(@NotNull String name) {
        SLOT_MAP.remove(name);
    }

    private @NotNull NBTTagCompound createFreshSlotTag() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString(PROFILE_NBT_KEY, NO_PROFILE_NBT_KEY);
        tag.setTag(CONFIG_NBT_KEY, new NBTTagCompound());

        return tag;
    }

    /**
     * Create a new, empty slot if a slot doesn't exist by the supplied name
     *
     * @param slotName the name of the slot to make
     */
    public void createNewSlot(@NotNull String slotName) {
        SLOT_MAP.computeIfAbsent(slotName, ignored -> createFreshSlotTag());
    }

    public @Nullable IMachineConfiguratorProfile getSlotProfile(@NotNull String name) {
        NBTTagCompound slot = getSlot(name);
        if (slot == null) return null;
        return ConfiguratorProfileRegistry.getConfiguratorProfileByName(slot.getString(PROFILE_NBT_KEY));
    }

    public void setSlotProfile(@NotNull String name, @NotNull IMachineConfiguratorProfile profile) {
        NBTTagCompound slot = getSlot(name);
        if (slot == null) return;
        slot.setString(PROFILE_NBT_KEY, profile.getName());
    }

    public @Nullable NBTTagCompound getSlotConfig(String name) {
        NBTTagCompound slot = getSlot(name);
        if (slot == null) return null;
        return slot.getCompoundTag(CONFIG_NBT_KEY);
    }

    public void setSlotConfig(@NotNull String name, @NotNull NBTTagCompound tag) {
        NBTTagCompound slot = getSlot(name);
        if (slot == null) return;
        slot.setTag(CONFIG_NBT_KEY, tag);
    }

    public Set<String> getSlotNames() {
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
