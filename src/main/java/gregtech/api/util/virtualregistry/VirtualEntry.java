package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;

public abstract class VirtualEntry implements INBTSerializable<NBTTagCompound> {

    protected static final String DEFAULT_COLOR = "FFFFFFFF";
    protected static final String NAME_KEY = "entry_name";
    protected static final String COLOR_KEY = "color";
    protected static final String ENTRY_KEY = "entry_tag";
    private @NotNull NBTTagCompound entry = new NBTTagCompound();

    public abstract EntryType getType();

    public String getColor() {
        return this.entry.getString(COLOR_KEY);
    }

    public void setColor(String color) {
        this.entry.setString(COLOR_KEY, color == null ? DEFAULT_COLOR : color);
    }

    public String getName() {
        return this.entry.getString(NAME_KEY);
    }

    protected void setName(String name) {
        this.entry.setString(NAME_KEY, name);
    }

    public NBTTagCompound getData() {
        if (!this.entry.hasKey(ENTRY_KEY))
            setData(new NBTTagCompound());

        return this.entry.getCompoundTag(ENTRY_KEY);
    }

    public void setData(NBTTagCompound entry) {
        this.entry.setTag(ENTRY_KEY, entry != null ? entry : new NBTTagCompound());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() &&
                this.getName().equals(other.getName());

    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.entry;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.entry = nbt;
    }
}
