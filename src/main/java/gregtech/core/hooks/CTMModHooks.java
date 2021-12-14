package gregtech.core.hooks;

import gregtech.api.util.GTLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import team.chisel.ctm.api.model.IModelCTM;
import team.chisel.ctm.client.model.ModelCTM;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class CTMModHooks {

    private static Field layers;

    static{
        try {
            layers = ModelCTM.class.getDeclaredField("layers");
            layers.setAccessible(true);
        } catch (NoSuchFieldException e) {
            GTLog.logger.error("CTMModHooks no such field");
        }
    }

    public static boolean canRenderInLayer(IModelCTM model, IBlockState state, BlockRenderLayer layer) {
        boolean flag = model.canRenderInLayer(state, layer);
        if (model instanceof ModelCTM && layers != null) {
            try {
                return CTMHooks.checkLayerWithOptiFine(flag, layers.getByte(model), layer);
            } catch (Exception ignored) {
                layers = null;
                GTLog.logger.error("CTMModHooks Field error");
            }
        }
        return flag;
    }
}
