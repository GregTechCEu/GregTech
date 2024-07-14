package gregtech.api.pattern;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * A possibly saner(and mutable) alternative to BlockPos, where getters and setters use indices and axis instead of
 * separate names to avoid stupid code
 */
public class GreggyBlockPos {

    protected final int[] pos;

    public GreggyBlockPos() {
        this(0, 0, 0);
    }

    public GreggyBlockPos(int x, int y, int z) {
        pos = new int[] { x, y, z };
    }

    public GreggyBlockPos(BlockPos base) {
        pos = new int[] { base.getX(), base.getY(), base.getZ() };
    }

    /**
     * Sets a coordinate in the given axis
     * 
     * @param axis  The axis to set
     * @param value The value of said coordinate
     */
    public GreggyBlockPos set(EnumFacing.Axis axis, int value) {
        pos[axis.ordinal()] = value;
        return this;
    }

    /**
     * Sets all 3 coordinates in the given axis order
     * 
     * @param a1 The first axis, p1 will be set from this
     * @param a2 The second axis, p2 will be set from this
     * @param p1 Value for a1
     * @param p2 Value for a2
     * @param p3 The axis is inferred from the other 2 axis(all 3 are unique, so if a1 is X and a2 is Z, then p3 is set
     *           in Y)
     */
    public GreggyBlockPos setAxisRelative(EnumFacing.Axis a1, EnumFacing.Axis a2, int p1, int p2, int p3) {
        set(a1, p1);
        set(a2, p2);
        // the 3 ordinals add up to 3, so to find the third axis just subtract the other 2 from 3
        pos[3 - a1.ordinal() - a2.ordinal()] = p3;
        return this;
    }

    /**
     * Offsets in the given {@link EnumFacing} amount times {@link BlockPos#offset(EnumFacing)}
     */
    public GreggyBlockPos offset(EnumFacing facing, int amount) {
        pos[0] += facing.getXOffset() * amount;
        pos[1] += facing.getYOffset() * amount;
        pos[2] += facing.getZOffset() * amount;
        return this;
    }

    /**
     * Sets this pos's position to be the same as the other one
     * @param other The other pos to get position from
     */
    public GreggyBlockPos from(GreggyBlockPos other) {
        System.arraycopy(other.pos, 0, this.pos, 0, 3);
        return this;
    }

    /**
     * Equivalent to calling {@link GreggyBlockPos#offset(EnumFacing, int)} with amount set to 1
     */
    public GreggyBlockPos offset(EnumFacing facing) {
        return offset(facing, 1);
    }

    /**
     * @return A new immutable instance of {@link BlockPos}
     */
    public BlockPos immutable() {
        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    /**
     * Gets a coordinate associated with the index, X = 0, Y = 1, Z = 2
     */
    public int get(int index) {
        return pos[index];
    }

    /**
     * Gets a coordinate associated with the axis
     */
    public int get(EnumFacing.Axis axis) {
        return pos[axis.ordinal()];
    }

    public GreggyBlockPos copy() {
        return new GreggyBlockPos().from(this);
    }
}
