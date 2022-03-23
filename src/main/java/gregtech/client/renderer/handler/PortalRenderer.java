package gregtech.client.renderer.handler;

import gregtech.api.GTValues;
import gregtech.common.entities.PortalEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class PortalRenderer extends Render<PortalEntity> {

    private static final ResourceLocation texture = new ResourceLocation(GTValues.MODID, "textures/entity/gtportal.png");
    protected PortalModel model = new PortalModel();

    public PortalRenderer(RenderManager renderManagerIn){
        super(renderManagerIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(PortalEntity entity) {
        return texture;
    }

    @Override
    public void doRender(PortalEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.bindEntityTexture(entity);
        GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.rotate( -entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.model.render(entity, partialTicks, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    public void setupTranslation(double x, double y, double z) {
        GlStateManager.translate((float)x, (float)y + 0.5F, (float)z);
    }

}
