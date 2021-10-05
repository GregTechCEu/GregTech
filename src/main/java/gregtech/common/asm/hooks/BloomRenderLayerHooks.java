package gregtech.common.asm.hooks;

import gregtech.api.render.shader.postprocessing.BloomEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/10/04
 * @Description:
 */
public class BloomRenderLayerHooks {
    public static BlockRenderLayer BLOOM;

    public static void preInit() {
        BLOOM = EnumHelper.addEnum(BlockRenderLayer.class, "BLOOM", new Class[]{String.class}, "Bloom");
    }

    public static void initBloomRenderLayer(BufferBuilder[] worldRenderers) {
        worldRenderers[BLOOM.ordinal()] = new BufferBuilder(131072);
    }

    public static int renderBloomBlockLayer(RenderGlobal renderglobal, BlockRenderLayer blockRenderLayer, double partialTicks, int pass, Entity entity) {
        BloomEffect.renderBloomLayer(renderglobal, (float) partialTicks, pass, entity);
        return renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
    }
}
