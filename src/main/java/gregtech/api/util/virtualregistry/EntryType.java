package gregtech.api.util.virtualregistry;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public enum EntryType implements IStringSerializable {
    ENDER_ITEM("ender_item"),
    ENDER_FLUID("ender_fluid"),
    ENDER_ENERGY("ender_energy");

    public static final EntryType[] VALUES = values();
    private final String name;

    EntryType(String name) {
        this.name = name.toLowerCase();
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
