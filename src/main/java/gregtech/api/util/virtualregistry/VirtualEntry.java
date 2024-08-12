package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;

public abstract class VirtualEntry implements INBTSerializable<NBTTagCompound> {

    public static final String DEFAULT_COLOR = "FFFFFFFF";
    protected static final String COLOR_KEY = "color";
    protected static final String DESC_KEY = "description";

    private int color = 0xFFFFFFFF;
    private String colorStr = DEFAULT_COLOR;
    private @NotNull String description = "";

    public abstract EntryTypes<? extends VirtualEntry> getType();

    public String getColorStr() {
        return colorStr;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = parseColor(color);
        this.colorStr = color.toUpperCase();
    }

    public void setColor(int color) {
        setColor(Integer.toHexString(color));
    }

    private int parseColor(String s) {
        // stupid java not having actual unsigned ints
        long tmp = Long.parseLong(s, 16);
        if (tmp > 0x7FFFFFFF) {
            tmp -= 0x100000000L;
        }
        return (int) tmp;
    }

    public @NotNull String getDescription() {
        return this.description;
    }

    public void setDescription(@NotNull String desc) {
        this.description = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() &&
                this.color == other.color;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        var tag = new NBTTagCompound();
        tag.setString(COLOR_KEY, this.colorStr);

        if (description != null && !description.isEmpty())
            tag.setString(DESC_KEY, this.description);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        setColor(nbt.getString(COLOR_KEY));

        if (nbt.hasKey(DESC_KEY))
            setDescription(nbt.getString(DESC_KEY));
    }
}
