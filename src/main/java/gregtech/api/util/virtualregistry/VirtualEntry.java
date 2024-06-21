package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;

public abstract class VirtualEntry implements INBTSerializable<NBTTagCompound> {

    protected static final String DEFAULT_COLOR = "FFFFFFFF";
    protected static final String NAME_KEY = "entry_name";
    protected static final String COLOR_KEY = "color";
    private final @NotNull NBTTagCompound data = new NBTTagCompound();

    public abstract <T extends VirtualEntry> EntryTypes<T> getType();

    public String getColor() {
        if (!this.data.hasKey(COLOR_KEY))
            setColor(DEFAULT_COLOR);

        return this.data.getString(COLOR_KEY);
    }

    public void setColor(String color) {
        this.data.setString(COLOR_KEY, color == null ? DEFAULT_COLOR : color.toUpperCase());
    }

    public String getName() {
        return this.data.getString(NAME_KEY);
    }

    protected void setName(String name) {
        this.data.setString(NAME_KEY, name == null || name.isEmpty() ? "null" : name);
    }

    @NotNull
    public final NBTTagCompound getData() {
        return this.data;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() &&
                this.getName().equals(other.getName());
    }

    @Override
    public final NBTTagCompound serializeNBT() {
        return this.data;
    }

    @Override
    public final void deserializeNBT(NBTTagCompound nbt) {
        for (var key : nbt.getKeySet()) {
            this.data.setTag(key, nbt.getTag(key));
        }
    }
}
