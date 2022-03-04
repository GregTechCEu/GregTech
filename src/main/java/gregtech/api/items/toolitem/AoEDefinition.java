package gregtech.api.items.toolitem;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class AoEDefinition {

    // Containerized
    public static AoEDefinition read(NBTTagCompound tag) {
        int column = 0, row = 0, layer = 0;
        boolean notNone = false;
        if (tag.hasKey("AOEColumn", Constants.NBT.TAG_INT)) {
            column = tag.getInteger("AOEColumn");
            notNone = true;
        }
        if (tag.hasKey("AOERow", Constants.NBT.TAG_INT)) {
            row = tag.getInteger("AOERow");
            notNone = true;
        }
        if (tag.hasKey("AOELayer", Constants.NBT.TAG_INT)) {
            layer = tag.getInteger("AOELayer");
            notNone = true;
        }
        return notNone ? new AoEDefinition(column, row, layer) : NONE;
    }

    private static final AoEDefinition NONE = new AoEDefinition();

    public static AoEDefinition of() {
        return NONE;
    }

    public static AoEDefinition of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? of() : new AoEDefinition(column, row, layer);
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
