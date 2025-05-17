package gregtech.api.util.virtualregistry;

import gregtech.api.util.GTLog;
import gregtech.api.util.virtualregistry.entries.VirtualTank;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

import static gregtech.api.util.GTUtility.gregtechId;

public final class EntryTypes<T extends VirtualEntry> {

    private static final Map<ResourceLocation, EntryTypes<?>> TYPES_MAP = new Object2ObjectOpenHashMap<>();
    public static final EntryTypes<VirtualTank> ENDER_FLUID = addEntryType(gregtechId("ender_fluid"), VirtualTank::new);
    public static final EntryTypes<VirtualChest> ENDER_ITEM = addEntryType(gregtechId("ender_item"), VirtualChest::new);
    // ENDER_ENERGY("ender_energy", null),
    // ENDER_REDSTONE("ender_redstone", null);
    private final ResourceLocation location;
    private final Supplier<T> factory;

    private EntryTypes(ResourceLocation location, Supplier<T> supplier) {
        this.location = location;
        this.factory = supplier;
    }

    public T createInstance(NBTTagCompound nbt) {
        var entry = createInstance();
        entry.deserializeNBT(nbt);
        return entry;
    }

    public T createInstance() {
        return factory.get();
    }

    @Override
    public String toString() {
        return this.location.toString();
    }

    @Nullable
    public static EntryTypes<? extends VirtualEntry> fromString(String name) {
        return TYPES_MAP.getOrDefault(gregtechId(name), null);
    }

    @Nullable
    public static EntryTypes<? extends VirtualEntry> fromLocation(String location) {
        return TYPES_MAP.getOrDefault(new ResourceLocation(location), null);
    }

    public static <E extends VirtualEntry> EntryTypes<E> addEntryType(ResourceLocation location, Supplier<E> supplier) {
        var type = new EntryTypes<>(location, supplier);
        if (!TYPES_MAP.containsKey(location)) {
            TYPES_MAP.put(location, type);
        } else {
            GTLog.logger.warn("Entry \"{}\" is already registered!", location);
        }
        return type;
    }
}
