package gregtech.api.items.toolitem;

import com.google.common.base.Preconditions;

public class AoEData {

    private static final AoEData NONE = new AoEData();

    public static AoEData of() {
        return NONE;
    }

    public static AoEData of(int height, int width, int depth) {
        Preconditions.checkArgument(height >= 1, "Height cannot be 0 or negative.");
        Preconditions.checkArgument(width >= 1, "Width cannot be 0 or negative.");
        Preconditions.checkArgument(depth >= 1, "Depth cannot be 0 or negative.");
        return height == 1 && width == 1 && depth == 1 ? of() : new AoEData(height, width, depth);
    }

    public final int height, width, depth;

    private AoEData() {
        this.height = 1;
        this.width = 1;
        this.depth = 1;
    }

    private AoEData(int height, int width, int depth) {
        this.height = height;
        this.width = width;
        this.depth = depth;
    }

}
