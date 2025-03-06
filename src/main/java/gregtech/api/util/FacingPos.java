package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class FacingPos {

    private final BlockPos pos;
    private final EnumFacing facing;
    private final int hashCode;

    public FacingPos(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
        this.hashCode = Objects.hash(pos, facing);
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockPos offset() {
        return pos.offset(facing);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacingPos facingPos = (FacingPos) o;
        return GTUtility.arePosEqual(pos, facingPos.getPos()) && facing == facingPos.getFacing();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
