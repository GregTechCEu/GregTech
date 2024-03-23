package gregtech.mixins.minecraft;

import gregtech.api.pollution.PollutionColorOverride;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.math.BlockPos;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    @ModifyExpressionValue(method = "colorMultiplier",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/renderer/color/IBlockColor;colorMultiplier(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;I)I"))
    public int gregtech$pollutionColorMultiplier(int color, @Local @NotNull IBlockState state, @Local @Nullable BlockPos pos) {
        if (pos == null) {
            return color;
        }

        final int dimension = Minecraft.getMinecraft().world.provider.getDimension();
        final Block block = state.getBlock();

        PollutionColorOverride override;
        if (block instanceof BlockLeaves) {
            override = PollutionColorOverride.LEAVES;
        } else if (block instanceof BlockTallGrass || block instanceof BlockFlower || block instanceof BlockDoublePlant) {
            override = PollutionColorOverride.FLOWER;
        } else if (block instanceof BlockGrass) {
            override = PollutionColorOverride.GRASS;
        } else if (state.getMaterial() == Material.WATER) {
            override = PollutionColorOverride.LIQUID;
        } else {
            return color;
        }

        int result = override.getColor(pos.getX(), pos.getZ(), dimension, color);
//        if (color != result) {
//            Minecraft.getMinecraft().world.markBlockRangeForRenderUpdate(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
//                    pos.getX(), pos.getY(), pos.getZ());
//        }

        return result;
    }
}
