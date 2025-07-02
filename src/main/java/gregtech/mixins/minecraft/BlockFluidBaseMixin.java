package gregtech.mixins.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockFluidBase.class)
public abstract class BlockFluidBaseMixin extends Block implements IFluidBlock {

    // 防止递归的标记
    private static final ThreadLocal<Boolean> isCalculatingFogColor = ThreadLocal.withInitial(() -> false);

    @Shadow(remap = false)
    protected int densityDir;

    @Shadow(remap = false)
    protected int density;

    @Shadow(remap = false)
    protected float quantaPerBlockFloat;

    protected BlockFluidBaseMixin(Material materialIn, MapColor color) {
        super(materialIn, color);
    }

    @Shadow(remap = false)
    protected abstract boolean isWithinFluid(IBlockAccess world, BlockPos pos, Vec3d vec);

    @Shadow(remap = false)
    protected abstract int getEffectiveQuanta(IBlockAccess world, BlockPos pos);

    /**
     * @reason 修复 getFogColor 和 getFilledPercentage 之间的递归调用问题
     * @author MeowmelMuku
     */
    @Overwrite(remap = false)
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks){
        // 检查是否已经在计算雾效颜色，防止递归
        if (isCalculatingFogColor.get()) {
            return originalColor;
        }

        try {
            isCalculatingFogColor.set(true);

            if (!isWithinFluid(world, pos, ActiveRenderInfo.projectViewFromEntity(entity, partialTicks))) {
                BlockPos otherPos = pos.down(densityDir);
                IBlockState otherState = world.getBlockState(otherPos);
                return otherState.getBlock()
                        .getFogColor(world, otherPos, otherState, entity, originalColor, partialTicks);
            }

            if (getFluid() != null) {
                int color = getFluid().getColor();
                float red = (color >> 16 & 0xFF) / 255.0F;
                float green = (color >> 8 & 0xFF) / 255.0F;
                float blue = (color & 0xFF) / 255.0F;
                return new Vec3d(red, green, blue);
            }

            return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
        } finally {
            isCalculatingFogColor.set(false);
        }
    }

    /**
     * @reason 修复 getFogColor 和 getFilledPercentage 之间的递归调用问题
     * @author MeowmelMuku
     */
    @Overwrite(remap = false)
    public float getFilledPercentage(IBlockAccess world, BlockPos pos)
    {
        // 如果正在计算雾效颜色，则返回默认值
        if (isCalculatingFogColor.get()) {
            return density > 0 ? 1 : -1; // 返回最大/最小值避免进一步计算
        }

        int quantaRemaining = getEffectiveQuanta(world, pos);
        float remaining = (quantaRemaining + 1f) / (quantaPerBlockFloat + 1f);
        return remaining * (density > 0 ? 1 : -1);
    }
}
