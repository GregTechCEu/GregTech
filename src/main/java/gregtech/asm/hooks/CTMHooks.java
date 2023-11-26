package gregtech.asm.hooks;

import gregtech.client.shader.Shaders;
import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class CTMHooks {

    public static ThreadLocal<Boolean> ENABLE = new ThreadLocal<>();

    public static boolean checkLayerWithOptiFine(boolean canRenderInLayer, byte layers, BlockRenderLayer layer) {
        if (Shaders.isOptiFineShaderPackLoaded()) {
            if (canRenderInLayer) {
                if (layer == BloomEffectUtil.getBloomLayer()) return false;
            } else if ((layers >> BloomEffectUtil.getBloomLayer().ordinal() & 1) == 1 &&
                    layer == BloomEffectUtil.getEffectiveBloomLayer()) {
                        return true;
                    }
        }
        return canRenderInLayer;
    }

    public static List<BakedQuad> getQuadsWithOptiFine(List<BakedQuad> ret, BlockRenderLayer layer,
                                                       IBakedModel bakedModel, IBlockState state, EnumFacing side,
                                                       long rand) {
        if (Shaders.isOptiFineShaderPackLoaded() && CTMHooks.ENABLE.get() == null) {
            if (layer == BloomEffectUtil.getBloomLayer()) {
                return Collections.emptyList();
            } else if (layer == BloomEffectUtil.getEffectiveBloomLayer()) {
                CTMHooks.ENABLE.set(true);
                List<BakedQuad> result = new ArrayList<>(ret);
                ForgeHooksClient.setRenderLayer(BloomEffectUtil.getBloomLayer());
                result.addAll(bakedModel.getQuads(state, side, rand));
                ForgeHooksClient.setRenderLayer(layer);
                CTMHooks.ENABLE.set(null);
                return result;
            }
        }
        return ret;
    }
}
