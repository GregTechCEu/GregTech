package gregtech.client.utils;

import gregtech.api.util.GTLog;
import gregtech.api.util.Mods;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;

import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.client.model.ModelCTM;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CTMHooks {

    private static Field layers;

    static {
        if (Mods.CTM.isModLoaded()) {
            try {
                layers = ModelCTM.class.getDeclaredField("layers");
                layers.setAccessible(true);
            } catch (NoSuchFieldException e) {
                GTLog.logger.error("CTMHooks no such field");
            }
        }
    }

    public static ThreadLocal<Boolean> ENABLE = new ThreadLocal<>();

    public static boolean checkLayerWithOptiFine(boolean canRenderInLayer, byte layers, BlockRenderLayer layer) {
        if (Mods.ShadersMod.isModLoaded()) {
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
        if (Mods.ShadersMod.isModLoaded() && CTMHooks.ENABLE.get() == null) {
            if (layer == BloomEffectUtil.getBloomLayer()) {
                return Collections.emptyList();
            } else if (layer == BloomEffectUtil.getEffectiveBloomLayer()) {
                CTMHooks.ENABLE.set(true);
                List<BakedQuad> result = new ArrayList<>(ret);
                ForgeHooksClient.setRenderLayer(BloomEffectUtil.getBloomLayer());
                result.addAll(bakedModel.getQuads(state, side, rand));
                ForgeHooksClient.setRenderLayer(layer);
                CTMHooks.ENABLE.remove();
                return result;
            }
        }
        return ret;
    }

    public static boolean canRenderInLayer(IModelCTM model, IBlockState state, BlockRenderLayer layer) {
        boolean canRenderInLayer = model.canRenderInLayer(state, layer);
        if (model instanceof ModelCTM && layers != null) {
            try {
                return CTMHooks.checkLayerWithOptiFine(canRenderInLayer, layers.getByte(model), layer);
            } catch (Exception ignored) {
                layers = null;
                GTLog.logger.error("CTMHooks Field error");
            }
        }
        return canRenderInLayer;
    }
}
