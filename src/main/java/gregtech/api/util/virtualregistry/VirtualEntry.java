package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;

public class VirtualEntry {

    private static final String DEFAULT_COLOR = "FFFFFFFF";
    private static final String NAME_KEY = "entry_name";
    private static final String COLOR_KEY = "color";
    private static final String TYPE_KEY = "entry_type";
    private static final String ENTRY_KEY = "entry_tag";
    private final NBTTagCompound entry;

    private VirtualEntry(EntryType type, String name, String color, NBTTagCompound entry) {
        var tag = new NBTTagCompound();
        tag.setByte(TYPE_KEY, (byte) type.ordinal());
        tag.setString(NAME_KEY, name);
        tag.setString(COLOR_KEY, color);
        tag.setTag(ENTRY_KEY, entry);
        this.entry = tag;
    }

    private VirtualEntry(NBTTagCompound entry) {
        this.entry = entry;
    }

    public static VirtualEntry of(EntryType type, String name, String color, NBTTagCompound entry) {
        return new VirtualEntry(type, name, color, entry);
    }

    public static VirtualEntry of(EntryType type, String name) {
        return new VirtualEntry(type, name, DEFAULT_COLOR, new NBTTagCompound());
    }

    public static VirtualEntry fromNBT(NBTTagCompound entry) {
        return new VirtualEntry(entry);
    }

    public EntryType getType() {
        return EntryType.VALUES[this.entry.getByte(TYPE_KEY)];
    }

    public String getColor() {
        return this.entry.getString(COLOR_KEY);
    }

    public void setColor(String color) {
        this.entry.setString(COLOR_KEY, color == null ? DEFAULT_COLOR : color);
    }

    public String getName() {
        return this.entry.getString(NAME_KEY);
    }

    public NBTTagCompound getEntry() {
        return this.entry.getCompoundTag(ENTRY_KEY);
    }

    public void setEntry(NBTTagCompound entry) {
        this.entry.setTag(ENTRY_KEY, entry == null ? new NBTTagCompound() : entry);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualEntry other)) return false;
        return this.getType() == other.getType() &&
                this.getName().equals(other.getName());

    }
}
