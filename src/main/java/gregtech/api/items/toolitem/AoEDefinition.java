package gregtech.api.items.toolitem;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class AoEDefinition {

    private static final AoEDefinition NONE = new AoEDefinition();

    public static AoEDefinition readMax(NBTTagCompound tag) {
        int column = 0, row = 0, layer = 0;
        if (tag.hasKey("AoEMaxColumn", Constants.NBT.TAG_INT)) {
            column = tag.getInteger("AoEMaxColumn");
        }
        if (tag.hasKey("AoEMaxRow", Constants.NBT.TAG_INT)) {
            row = tag.getInteger("AoEMaxRow");
        }
        if (tag.hasKey("AoEMaxLayer", Constants.NBT.TAG_INT)) {
            layer = tag.getInteger("AoEMaxLayer");
        }
        return column == 0 && row == 0 && layer == 0 ? NONE : new AoEDefinition(column, row, layer);
    }

    public static AoEDefinition read(NBTTagCompound tag, @Nullable AoEDefinition defaultDefinition) {
        int column, row, layer;
        if (tag.hasKey("AoEColumn", Constants.NBT.TAG_INT)) {
            column = tag.getInteger("AoEColumn");
        } else {
            column = defaultDefinition == null ? 0 : defaultDefinition.column;
            tag.setInteger("AoEColumn", column);
        }
        if (tag.hasKey("AoERow", Constants.NBT.TAG_INT)) {
            row = tag.getInteger("AoERow");
        } else {
            row = defaultDefinition == null ? 0 : defaultDefinition.row;
            tag.setInteger("AoERow", row);
        }
        if (tag.hasKey("AoELayer", Constants.NBT.TAG_INT)) {
            layer = tag.getInteger("AoELayer");
        } else {
            layer = defaultDefinition == null ? 0 : defaultDefinition.layer;
            tag.setInteger("AoELayer", layer);
        }
        return column == 0 && row == 0 && layer == 0 ? NONE : new AoEDefinition(column, row, layer);
    }

    public static int getColumn(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (tag.hasKey("AoEColumn", Constants.NBT.TAG_INT)) {
            return tag.getInteger("AoEColumn");
        }
        return defaultDefinition.column;
    }

    public static int getRow(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (tag.hasKey("AoERow", Constants.NBT.TAG_INT)) {
            return tag.getInteger("AoERow");
        }
        return defaultDefinition.row;
    }

    public static int getLayer(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (tag.hasKey("AoELayer", Constants.NBT.TAG_INT)) {
            return tag.getInteger("AoELayer");
        }
        return defaultDefinition.layer;
    }

    public static void increaseColumn(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (!tag.hasKey("AoEColumn", Constants.NBT.TAG_INT)) {
            tag.setInteger("AoEColumn", defaultDefinition.column);
        } else {
            int currentColumn = tag.getInteger("AoEColumn");
            if (currentColumn < defaultDefinition.column) {
                tag.setInteger("AoEColumn", currentColumn + 1);
            }
        }
    }

    public static void increaseRow(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (!tag.hasKey("AoERow", Constants.NBT.TAG_INT)) {
            tag.setInteger("AoERow", defaultDefinition.row);
        } else {
            int currentRow = tag.getInteger("AoERow");
            if (currentRow < defaultDefinition.row) {
                tag.setInteger("AoERow", currentRow + 1);
            }
        }
    }

    public static void increaseLayer(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (!tag.hasKey("AoELayer", Constants.NBT.TAG_INT)) {
            tag.setInteger("AoELayer", defaultDefinition.layer);
        } else {
            int currentLayer = tag.getInteger("AoELayer");
            if (currentLayer < defaultDefinition.layer) {
                tag.setInteger("AoELayer", currentLayer + 1);
            }
        }
    }

    public static void decreaseColumn(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (!tag.hasKey("AoEColumn", Constants.NBT.TAG_INT)) {
            tag.setInteger("AoEColumn", defaultDefinition.column);
        } else {
            int currentColumn = tag.getInteger("AoEColumn");
            if (currentColumn > 0) {
                tag.setInteger("AoEColumn", currentColumn - 1);
            }
        }
    }

    public static void decreaseRow(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (!tag.hasKey("AoERow", Constants.NBT.TAG_INT)) {
            tag.setInteger("AoERow", defaultDefinition.row);
        } else {
            int currentRow = tag.getInteger("AoERow");
            if (currentRow > 0) {
                tag.setInteger("AoERow", currentRow - 1);
            }
        }
    }

    public static void decreaseLayer(NBTTagCompound tag, AoEDefinition defaultDefinition) {
        if (!tag.hasKey("AoELayer", Constants.NBT.TAG_INT)) {
            tag.setInteger("AoELayer", defaultDefinition.layer);
        } else {
            int currentLayer = tag.getInteger("AoELayer");
            if (currentLayer > 0) {
                tag.setInteger("AoELayer", currentLayer - 1);
            }
        }
    }

    public static AoEDefinition none() {
        return NONE;
    }

    public static AoEDefinition of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? NONE : new AoEDefinition(column, row, layer);
    }

    public final int column, row, layer;

    private AoEDefinition() {
        this.column = 0;
        this.row = 0;
        this.layer = 0;
    }

    private AoEDefinition(int column, int row, int layer) {
        this.column = column;
        this.row = row;
        this.layer = layer;
    }

}
