package gregtech.mixins.minecraft;

import gregtech.client.model.AOAccessor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.BitSet;

@Mixin(targets = "net.minecraft.client.renderer.BlockModelRenderer$AmbientOcclusionFace")
public abstract class AOMixin implements AOAccessor {

    @Shadow
    @Final
    private float[] vertexColorMultiplier = new float[4];

    @Shadow
    @Final
    private int[] vertexBrightness = new int[4];

    @Shadow
    public abstract void updateVertexBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos,
                                                EnumFacing direction, float[] faceShape, BitSet shapeState);

    @Override
    public void gregTech$updateBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos,
                                          EnumFacing direction,
                                          float[] faceShape, BitSet shapeState) {
        this.updateVertexBrightness(worldIn, state, centerPos, direction, faceShape, shapeState);
    }

    @Override
    public float[] gregTech$getColorMultiplier() {
        return vertexColorMultiplier;
    }

    @Override
    public int[] gregTech$getBrightness() {
        return vertexBrightness;
    }
}
