package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class BlockPosFace {
    public final EnumFacing facing;
    public final BlockPos pos;

    public BlockPosFace(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
    }

    @Override
    public boolean equals(@Nullable Object bp) {
        if (bp instanceof BlockPosFace) {
            return pos == ((BlockPosFace) bp).pos && ((BlockPosFace) bp).facing == facing;
        }
        return super.equals(bp);
    }

    @Override
    public int hashCode() {
        return pos.hashCode() + facing.hashCode();
    }
}
