package gregtech.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.BitSet;

public interface AOAccessor {

    void gregTech$updateBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos,
                                   EnumFacing direction, float[] faceShape, BitSet shapeState);

    float[] gregTech$getColorMultiplier();

    int[] gregTech$getBrightness();
}
