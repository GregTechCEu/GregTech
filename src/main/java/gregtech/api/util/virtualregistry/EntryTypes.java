package gregtech.api.util.virtualregistry;

import gregtech.api.util.virtualregistry.entries.VirtualTank;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class EntryTypes<T extends VirtualEntry> {

    private static final Map<String, EntryTypes<?>> TYPES_MAP = new HashMap<>();
    public static final EntryTypes<VirtualTank> ENDER_FLUID = new EntryTypes<>("ender_fluid", VirtualTank::new);
    // ENDER_ITEM("ender_item", null),
    // ENDER_ENERGY("ender_energy", null),
    // ENDER_REDSTONE("ender_redstone", null);
    private final String name;
    private final Supplier<T> factory;

    private EntryTypes(String name, Supplier<T> supplier) {
        this.name = name.toLowerCase();
        this.factory = supplier;
        if (!TYPES_MAP.containsKey(name.toLowerCase()))
            TYPES_MAP.put(this.name, this);
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
        return this.name;
    }

    @Nullable
    public static EntryTypes<? extends VirtualEntry> fromString(String name) {
        return TYPES_MAP.get(name);
    }
}
