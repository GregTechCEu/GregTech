package gregtech.mixins.ctm;

import org.spongepowered.asm.mixin.Mixin;
import team.chisel.ctm.client.asm.CTMCoreMethods;

@Mixin(CTMCoreMethods.class)
public class CTMRenderInLayerMixin {

    /*
     * @ModifyExpressionValue(method = "canRenderInLayer", at = @At(value = "INVOKE_ASSIGN", target =
     * "Lteam/chisel/ctm/api/model/IModelCTM;canRenderInLayer(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z"
     * ), remap = false)
     * private static boolean renderInLayer(boolean originalResult, @Nonnull IBlockState state, @Nonnull
     * BlockRenderLayer layer) {
     *
     * byte layers = ((ModelCTMLayersAccessor) ModelCTM.getInstance()).getLayers();
     * if (model instanceof ModelCTM && layers != null) {
     * try {
     * return CTMHooks.checkLayerWithOptiFine(originalResult, layers.getByte(model), layer);
     * } catch (Exception ignored) {
     * layers = null;
     * GTLog.logger.error("CTMModHooks Field error");
     * }
     * }
     * return originalResult;
     * }
     */
}
