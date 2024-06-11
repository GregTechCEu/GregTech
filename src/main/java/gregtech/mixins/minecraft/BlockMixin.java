package gregtech.mixins.minecraft;

import gregtech.api.util.Mods;
import gregtech.asm.hooks.BlockHooks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.Loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Apply our block hooks for our custom models when CTM is not loaded
 */
@Mixin(Block.class)
public class BlockMixin {

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
