package gregtech.api.block;

import gregtech.api.util.GTLog;
import gregtech.client.renderer.GTRendererState;
import gregtech.client.renderer.texture.RenderContext;

public interface IBlockRenderer {

    default boolean renderBlockSafe(GTRendererState rendererState, RenderContext context) {
        try {
            renderBlock(rendererState, context);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
        return true;
    }

    void renderBlock(GTRendererState rendererState, RenderContext context);
}
