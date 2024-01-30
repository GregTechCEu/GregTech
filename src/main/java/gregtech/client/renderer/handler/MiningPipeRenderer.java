package gregtech.client.renderer.handler;

import gregtech.client.utils.MinerRenderHelper;
import gregtech.common.entities.MiningPipeEntity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiningPipeRenderer extends Render<MiningPipeEntity<?>> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Render<MiningPipeEntity> iHateJavaGenerics(RenderManager manager) {
        return (Render<MiningPipeEntity>) (Render) new MiningPipeRenderer(manager);
    }

    public MiningPipeRenderer(RenderManager manager) {
        super(manager);
        this.shadowSize = 0;
    }

    @Override
    public void doRender(@NotNull MiningPipeEntity<?> entity, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        boolean renderOutlines = this.renderOutlines;
        if (renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        MinerRenderHelper.renderPipe(x, y, z, partialTicks, entity);

        if (renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@NotNull MiningPipeEntity<?> entity) {
        return null;
    }
}
