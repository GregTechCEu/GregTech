package gregtech.api.pattern;

import gregtech.api.util.RelativeDirection;

import net.minecraft.util.EnumFacing;

/**
 * Simple class for a relative offset from a position.
 */
public class OriginOffset {

    protected final int[] offset = new int[3];

    public OriginOffset move(RelativeDirection dir, int amount) {
        amount *= (dir.ordinal() % 2 == 0) ? 1 : -1;
        offset[dir.ordinal() / 2] += amount;
        return this;
    }

    public OriginOffset move(RelativeDirection dir) {
        return move(dir, 1);
    }

    public int get(RelativeDirection dir) {
        return offset[dir.ordinal() / 2] * ((dir.ordinal() % 2 == 0) ? 1 : -1);
    }

    public void apply(GreggyBlockPos pos, EnumFacing frontFacing, EnumFacing upFacing) {
        for (int i = 0; i < 3; i++) {
            pos.offset(RelativeDirection.VALUES[2 * i].getRelativeFacing(frontFacing, upFacing), offset[i]);
        }
    }
}
