package gregtech.asm.hooks;

import gregtech.client.model.customtexture.CustomTextureBakedModel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
import net.minecraft.util.BlockRenderLayer;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BlockHooks {

    public static boolean ENABLE = true;

    public static Boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        if (ENABLE) {
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
            if (model instanceof WeightedBakedModel) {
                model = ((WeightedBakedModel) model).baseModel;
            }

            Boolean ret;
            if (model instanceof CustomTextureBakedModel) {
                ret = ((CustomTextureBakedModel) model).getModel().canRenderInLayer(state, layer);
            } else {
                ret = null;
            }
            return ret;
        }
        return null;
    }
}
