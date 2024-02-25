package gregtech.mixins.ctm;

import gregtech.asm.hooks.CTMModHooks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import team.chisel.ctm.client.asm.CTMCoreMethods;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

@Mixin(CTMCoreMethods.class)
public class CTMRenderInLayerMixin {

    @ModifyExpressionValue(method = "canRenderInLayer",
                           at = @At(value = "INVOKE_ASSIGN",
                                    target = "Lteam/chisel/ctm/api/model/IModelCTM;canRenderInLayer(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z"),
                           remap = false)
    private static Boolean checkRenderInLayer(Boolean originalResult, @NotNull IBlockState state,
                                              @NotNull BlockRenderLayer layer, @Local(ordinal = 0) IBakedModel model) {
        return CTMModHooks.canRenderInLayer(((AbstractCTMBakedModel) model).getModel(), state, layer);
    }
}
