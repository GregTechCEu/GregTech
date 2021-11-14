package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class BlockPosFace extends BlockPos {
    public final EnumFacing facing;

    public BlockPosFace(BlockPos pos, EnumFacing facing) {
        super(pos);
        this.facing = facing;
    }

    @Override
    public boolean equals(@Nullable Object bp) {
        if (bp instanceof BlockPosFace) {
            return super.equals(bp) && ((BlockPosFace) bp).facing == facing;
        }
        return super.equals(bp);
    }
}
