package gregtech.api.pattern.pattern;

import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import net.minecraft.util.EnumFacing;

import java.util.List;

/**
 * A strategy to how aisles should be checked in patterns.
 */
public abstract class AisleStrategy {

    protected final int[] dimensions = new int[3];
    protected final RelativeDirection[] directions = new RelativeDirection[3];

    protected BlockPattern pattern;
    protected GreggyBlockPos pos;
    protected EnumFacing front, up;

    /**
     * Checks the aisles
     * 
     * @param flip Whether this is a flipped pattern check.
     * @return Whether the pattern is formed after this.
     */
    public abstract boolean check(boolean flip);

    /**
     * Called at the start of a structure check.
     */
    protected void start(GreggyBlockPos pos, EnumFacing front, EnumFacing up) {
        this.pos = pos;
        this.front = front;
        this.up = up;
    }

    /**
     * No more aisles will be added. Check preconditions and throw exceptions here.
     */
    protected void finish(int[] dimensions, RelativeDirection[] directions, List<PatternAisle> aisles) {
        System.arraycopy(dimensions, 0, this.dimensions, 0, 3);
        System.arraycopy(directions, 0, this.directions, 0, 3);
    }

    protected boolean checkAisle(int index, int offset, boolean flip) {
        GTLog.logger.info("Checked aisle {} with offset {} and flip {}", index, offset, flip);
        return pattern.checkAisle(pos, front, up, index, offset, flip);
    }
}
