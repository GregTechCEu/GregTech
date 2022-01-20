package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class FacingPos {
    private final BlockPos pos;
    private final EnumFacing facing;

    public FacingPos(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
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
        FacingPos facingPos = (FacingPos) o;
        return GTUtility.arePosEqual(facingPos.pos, pos) && facing == facingPos.facing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, facing);
    }
}
