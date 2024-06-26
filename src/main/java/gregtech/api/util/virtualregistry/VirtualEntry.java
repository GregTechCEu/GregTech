package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;

public abstract class VirtualEntry implements INBTSerializable<NBTTagCompound> {

    public static final String DEFAULT_COLOR = "FFFFFFFF";
    protected static final String COLOR_KEY = "color";
    private final @NotNull NBTTagCompound data = new NBTTagCompound();

    public abstract EntryTypes<? extends VirtualEntry> getType();

    public String getColor() {
        if (!this.data.hasKey(COLOR_KEY))
            setColor(DEFAULT_COLOR);

        return this.data.getString(COLOR_KEY);
    }

    public void setColor(String color) {
        this.data.setString(COLOR_KEY, color == null ? DEFAULT_COLOR : color.toUpperCase());
    }

    @NotNull
    protected final NBTTagCompound getData() {
        return this.data;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() &&
                this.getData().equals(other.getData());
    }

    @Override
    public final NBTTagCompound serializeNBT() {
        return this.data;
    }

    @Override
    public final void deserializeNBT(NBTTagCompound nbt) {
        this.data.merge(nbt);
    }
}
