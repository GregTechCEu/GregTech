package gregtech.api.pattern.pattern;

import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Matrix4f;

import java.util.List;
import java.util.Map;

/**
 * A strategy to how aisles should be checked in patterns.
 */
public abstract class AisleStrategy {

    protected final int[] dimensions = new int[3];
    protected final EnumFacing[] directions = new EnumFacing[3];

    protected BlockPattern pattern;
    protected Matrix4f transform;

    /**
     * Checks the aisles
     *
     * @return Whether the pattern is formed after this.
     */
    public abstract boolean check();

    /**
     * Gets the order in which aisles should be displayed, or built in case of autobuild.
     * 
     * @param map The map, the same one that is passed through
     *            {@link gregtech.api.metatileentity.multiblock.MultiblockControllerBase#autoBuild(EntityPlayer, Map, String)}
     * @return Array where the i-th element specifies that at offset i there would be aisle a_i
     */
    public abstract int @NotNull [] getDefaultAisles(@Nullable Map<String, String> map);

    /**
     * Called at the start of a structure check.
     */
    protected void start(Matrix4f transform) {
        this.transform = transform;
    }

    /**
     * No more aisles will be added. Check preconditions and throw exceptions here.
     */
    protected void finish(int[] dimensions, EnumFacing[] directions, List<PatternAisle> aisles) {
        System.arraycopy(dimensions, 0, this.dimensions, 0, 3);
        System.arraycopy(directions, 0, this.directions, 0, 3);
    }

    protected boolean checkAisle(int index, int offset) {
        // todo remove
        GTLog.logger.info("Checked aisle {} with offset {}", index, offset);
        return pattern.checkAisle(transform, index, offset);
    }
}
