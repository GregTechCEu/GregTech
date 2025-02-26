package gregtech.api.util;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class DimensionPos {

    private final BlockPos pos;
    private final int dimension;
    private final int hashCode;

    public DimensionPos(BlockPos pos, int dimension) {
        this.pos = pos;
        this.dimension = dimension;
        this.hashCode = Objects.hash(pos, dimension);
    }

    public int getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionPos dimensionPos = (DimensionPos) o;
        return GTUtility.arePosEqual(pos, dimensionPos.getPos()) && dimension == dimensionPos.getDimension();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
