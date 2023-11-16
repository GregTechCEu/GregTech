package gregtech.client.renderer.handler;

import gregtech.common.entities.MiningPipeEntity;
import gregtech.client.utils.MinerRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MiningPipeRenderer extends Render<MiningPipeEntity<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Render<MiningPipeEntity> iHateJavaGenerics(RenderManager manager) {
        return (Render<MiningPipeEntity>) (Render) new MiningPipeRenderer(manager);
    }

    public MiningPipeRenderer(RenderManager manager) {
        super(manager);

        this.shadowSize = 0;
    }

    @Override
    public void doRender(@Nonnull MiningPipeEntity<?> entity, double x, double y, double z, float entityYaw, float partialTicks) {
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
    protected ResourceLocation getEntityTexture(@Nonnull MiningPipeEntity<?> entity) {
        return null;
    }
}
