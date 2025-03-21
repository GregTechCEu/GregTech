package gregtech.api.block;

import gregtech.api.util.GTLog;
import gregtech.client.renderer.GTRendererState;

public interface IBlockRenderer {

    default boolean renderBlockSafe(GTRendererState rendererState) {
        try {
            renderBlock(rendererState);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
        return true;
    }

    void renderBlock(GTRendererState rendererState);
}
