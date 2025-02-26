package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class DimensionFacingPos {

    private final BlockPos pos;
    private final EnumFacing facing;
    private final int dimension;
    private final int hashCode;

    public DimensionFacingPos(BlockPos pos, EnumFacing facing, int dimension) {
        this.pos = pos;
        this.facing = facing;
        this.dimension = dimension;
        this.hashCode = Objects.hash(pos, facing, dimension);
    }

    public int getDimension() {
        return dimension;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionFacingPos that = (DimensionFacingPos) o;
        return dimension == that.dimension && Objects.equals(pos, that.pos) &&
                facing == that.facing;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
