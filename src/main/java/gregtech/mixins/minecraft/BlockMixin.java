package gregtech.mixins.minecraft;

import gregtech.api.GTValues;
import gregtech.asm.hooks.BlockHooks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.Loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO, test to make sure this works when CTM is loaded
// TODO, put into separate refmap, so it can queue when CTM is not loaded

/**
 * Apply our block hooks for our custom models when CTM is not loaded
 */
@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "canRenderInLayer", at = @At("HEAD"), cancellable = true, remap = false)
    private void canRenderInLayer(IBlockState state, BlockRenderLayer layer, CallbackInfoReturnable<Boolean> cir) {
        if (!Loader.instance().getIndexedModList().containsKey(GTValues.MODID_CTM)) {
            Boolean result = BlockHooks.canRenderInLayer(state, layer);

            if (result != null) {
                cir.setReturnValue(result);
                cir.cancel();
            }
        }
    }
}
