package gregtech.mixins.minecraft;

import gregtech.api.util.Mods;
import gregtech.client.utils.BlockHooks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import static gregtech.common.ConfigHolder.vanillaOptimizeOptions;

/**
 * Apply our block hooks for our custom models when CTM is not loaded
 */
@Mixin(Block.class)
public class BlockMixin {
    /**
     * 完全禁用方块随机刻以优化性能
     * @author MeowmelMuku
     * @reason 性能优化
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void injectRandomTick(World worldIn, BlockPos pos, IBlockState state, Random random, CallbackInfo ci) {
        if (vanillaOptimizeOptions.disableUpdateEnable) {
            ci.cancel(); // 跳过原方法逻辑
        }
    }

    @Inject(method = "canRenderInLayer", at = @At("HEAD"), cancellable = true, remap = false)
    private void canRenderInLayer(IBlockState state, BlockRenderLayer layer, CallbackInfoReturnable<Boolean> cir) {
        if (!Loader.instance().getIndexedModList().containsKey(Mods.Names.CONNECTED_TEXTURES_MOD)) {
            Boolean result = BlockHooks.canRenderInLayer(state, layer);

            if (result != null) {
                cir.setReturnValue(result);
                cir.cancel();
            }
        }
    }
}
