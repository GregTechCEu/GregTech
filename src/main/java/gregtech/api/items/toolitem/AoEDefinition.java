package gregtech.api.items.toolitem;

import com.google.common.base.Preconditions;

public class AoEDefinition {

    private static final AoEDefinition NONE = new AoEDefinition();

    public static AoEDefinition of() {
        return NONE;
    }

    public static AoEDefinition of(int height, int width, int depth) {
        Preconditions.checkArgument(height >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(width >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(depth >= 0, "Depth cannot be negative.");
        return height == 0 && width == 0 && depth == 0 ? of() : new AoEDefinition(height, width, depth);
    }

    public final int height, width, depth;

    private AoEDefinition() {
        this.height = 0;
        this.width = 0;
        this.depth = 0;
    }

    private AoEDefinition(int height, int width, int depth) {
        this.height = height;
        this.width = width;
        this.depth = depth;
    }

}
