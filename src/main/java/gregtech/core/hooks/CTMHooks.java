package gregtech.core.hooks;

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

    public static boolean checkLayerWithOptiFine(boolean flag, byte layers, BlockRenderLayer layer) {
        if (Shaders.isOptiFineShaderPackLoaded()) {
            if (flag) {
                if (layer == BloomEffectUtil.BLOOM) return false;
            } else if (((layers >> BloomEffectUtil.BLOOM.ordinal()) & 1) == 1 && layer == BloomEffectUtil.getRealBloomLayer()) {
                return true;
            }
        }
        return flag;
    }

    public static List<BakedQuad> getQuadsWithOptiFine(List<BakedQuad> ret, BlockRenderLayer layer, IBakedModel bakedModel, IBlockState state, EnumFacing side, long rand) {
        if (Shaders.isOptiFineShaderPackLoaded() && CTMHooks.ENABLE.get() == null) {
            if (layer == BloomEffectUtil.BLOOM) {
                return Collections.emptyList();
            } else if (layer == BloomEffectUtil.getRealBloomLayer()) {
                CTMHooks.ENABLE.set(true);
                List<BakedQuad> result = new ArrayList<>(ret);
                ForgeHooksClient.setRenderLayer(BloomEffectUtil.BLOOM);
                result.addAll(bakedModel.getQuads(state, side, rand));
                ForgeHooksClient.setRenderLayer(layer);
                CTMHooks.ENABLE.set(null);
                return result;
            }
        }
        return ret;
    }
}
