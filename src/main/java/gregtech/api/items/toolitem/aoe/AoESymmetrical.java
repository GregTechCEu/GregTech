package gregtech.api.items.toolitem.aoe;

import gregtech.api.items.toolitem.ToolHelper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

public class AoESymmetrical {

    private static final AoESymmetrical NONE = new AoESymmetrical();

    public static AoESymmetrical readMax(NBTTagCompound tag) {
        int column = 0, row = 0, layer = 0;
        if (tag.hasKey(ToolHelper.MAX_AOE_COLUMN_KEY, Constants.NBT.TAG_INT)) {
            column = tag.getInteger(ToolHelper.MAX_AOE_COLUMN_KEY);
        }
        if (tag.hasKey(ToolHelper.MAX_AOE_ROW_KEY, Constants.NBT.TAG_INT)) {
            row = tag.getInteger(ToolHelper.MAX_AOE_ROW_KEY);
        }
        if (tag.hasKey(ToolHelper.MAX_AOE_LAYER_KEY, Constants.NBT.TAG_INT)) {
            layer = tag.getInteger(ToolHelper.MAX_AOE_LAYER_KEY);
        }
        return column == 0 && row == 0 && layer == 0 ? NONE : new AoESymmetrical(column, row, layer);
    }

    public static AoESymmetrical read(NBTTagCompound tag, @Nullable AoESymmetrical defaultDefinition) {
        int column, row, layer;
        if (tag.hasKey(ToolHelper.AOE_COLUMN_KEY, Constants.NBT.TAG_INT)) {
            column = tag.getInteger(ToolHelper.AOE_COLUMN_KEY);
        } else {
            column = defaultDefinition == null ? 0 : defaultDefinition.column;
        }
        if (tag.hasKey(ToolHelper.AOE_ROW_KEY, Constants.NBT.TAG_INT)) {
            row = tag.getInteger(ToolHelper.AOE_ROW_KEY);
        } else {
            row = defaultDefinition == null ? 0 : defaultDefinition.row;
        }
        if (tag.hasKey(ToolHelper.AOE_LAYER_KEY, Constants.NBT.TAG_INT)) {
            layer = tag.getInteger(ToolHelper.AOE_LAYER_KEY);
        } else {
            layer = defaultDefinition == null ? 0 : defaultDefinition.layer;
        }
        if (column == 0 && row == 0 && layer == 0) {
            return NONE;
        }
        tag.setInteger(ToolHelper.AOE_COLUMN_KEY, column);
        tag.setInteger(ToolHelper.AOE_ROW_KEY, row);
        tag.setInteger(ToolHelper.AOE_LAYER_KEY, layer);
        return new AoESymmetrical(column, row, layer);
    }

    public static int getColumn(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        if (tag.hasKey(ToolHelper.AOE_COLUMN_KEY, Constants.NBT.TAG_INT)) {
            return tag.getInteger(ToolHelper.AOE_COLUMN_KEY);
        }
        return defaultDefinition.column;
    }

    public static int getRow(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        if (tag.hasKey(ToolHelper.AOE_ROW_KEY, Constants.NBT.TAG_INT)) {
            return tag.getInteger(ToolHelper.AOE_ROW_KEY);
        }
        return defaultDefinition.row;
    }

    public static int getLayer(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        if (tag.hasKey(ToolHelper.AOE_LAYER_KEY, Constants.NBT.TAG_INT)) {
            return tag.getInteger(ToolHelper.AOE_LAYER_KEY);
        }
        return defaultDefinition.layer;
    }

    public static void setColumn(NBTTagCompound tag, int i, AoESymmetrical defaultDefinition) {
        tag.setInteger(ToolHelper.AOE_COLUMN_KEY, MathHelper.clamp(i, 0, defaultDefinition.column));
    }

    public static void setRow(NBTTagCompound tag, int i, AoESymmetrical defaultDefinition) {
        tag.setInteger(ToolHelper.AOE_ROW_KEY, MathHelper.clamp(i, 0, defaultDefinition.row));
    }

    public static void setLayer(NBTTagCompound tag, int i, AoESymmetrical defaultDefinition) {
        tag.setInteger(ToolHelper.AOE_LAYER_KEY, MathHelper.clamp(i, 0, defaultDefinition.layer));
    }

    public static void increaseColumn(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        int currentColumn = getColumn(tag, defaultDefinition);
        setColumn(tag, currentColumn + 1, defaultDefinition);
    }

    public static void increaseRow(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        int currentRow = getRow(tag, defaultDefinition);
        setRow(tag, currentRow + 1, defaultDefinition);
    }

    public static void increaseLayer(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        int currentLayer = getLayer(tag, defaultDefinition);
        setLayer(tag, currentLayer + 1, defaultDefinition);
    }

    public static void decreaseColumn(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        int currentColumn = getColumn(tag, defaultDefinition);
        setColumn(tag, currentColumn - 1, defaultDefinition);
    }

    public static void decreaseRow(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        int currentRow = getRow(tag, defaultDefinition);
        setRow(tag, currentRow + 1, defaultDefinition);
    }

    public static void decreaseLayer(NBTTagCompound tag, AoESymmetrical defaultDefinition) {
        int currentLayer = getLayer(tag, defaultDefinition);
        setLayer(tag, currentLayer + 1, defaultDefinition);
    }

    public static AoESymmetrical none() {
        return NONE;
    }

    public static AoESymmetrical of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? NONE : new AoESymmetrical(column, row, layer);
    }

    public final int column, row, layer;

    private AoESymmetrical() {
        this.column = 0;
        this.row = 0;
        this.layer = 0;
    }

    private AoESymmetrical(int column, int row, int layer) {
        this.column = column;
        this.row = row;
        this.layer = layer;
    }
}
