package gregtech.api.pattern.pattern;

import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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
     * Gets the order in which aisles should be displayed, or built in case of autobuild.
     * 
     * @param map The map, the same one that is passed through
     *            {@link gregtech.api.metatileentity.multiblock.MultiblockControllerBase#autoBuild(EntityPlayer, Map)}
     * @return Array where the i-th element specifies that at offset i there would be aisle a_i
     */
    public abstract int @NotNull [] getDefaultAisles(@Nullable Map<String, String> map);

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
        // todo remove
        GTLog.logger.info("Checked aisle {} with offset {} and flip {}", index, offset, flip);
        return pattern.checkAisle(pos, front, up, index, offset, flip);
    }
}
