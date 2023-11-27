package gregtech.client.renderer.handler;

import gregtech.api.util.GTUtility;
import gregtech.common.entities.PortalEntity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class PortalRenderer extends Render<PortalEntity> {

    private static final ResourceLocation texture = GTUtility.gregtechId("textures/entity/gtportal.png");
    protected PortalModel model = new PortalModel();

    public PortalRenderer(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@NotNull PortalEntity entity) {
        return texture;
    }

    @Override
    public void doRender(@NotNull PortalEntity entity, double x, double y, double z, float entityYaw,
                         float partialTicks) {
        GlStateManager.pushMatrix();
        PortalRenderer.setupTranslation(x, y, z);
        this.bindEntityTexture(entity);
        float scaleX = 0.0625F, scaleY = 0.0625F, scaleZ = 0.0625F;
        float translateY = 0.F;
        if (entity.isOpening()) {
            if (entity.getTimeToDespawn() <= 195) {
                scaleY *= MathHelper.clamp((195.F - entity.getTimeToDespawn() + partialTicks) / 5.F, 0.05F, 1.F);
                translateY = 0.5F *
                        (1.F - MathHelper.clamp((195.F - entity.getTimeToDespawn() + partialTicks) / 5.F, 0.F, 1.F));
            } else {
                scaleX *= MathHelper.clamp((200.F - entity.getTimeToDespawn() + partialTicks) / 5.F, 0.05F, 1.F);
                scaleY *= 0.05F;
                scaleZ *= MathHelper.clamp((200.F - entity.getTimeToDespawn() + partialTicks) / 5.F, 0.05F, 1.F);
                translateY = 0.5F;
            }
        } else if (entity.isClosing()) {
            if (entity.getTimeToDespawn() >= 5) {
                scaleY *= MathHelper.clamp((entity.getTimeToDespawn() - partialTicks - 5.F) / 5.F, 0.05F, 1.F);
                translateY = 0.5F *
                        (1.F - MathHelper.clamp((entity.getTimeToDespawn() - partialTicks - 5.F) / 5.F, 0.F, 1.F));
            } else {
                scaleX *= MathHelper.clamp((entity.getTimeToDespawn() - partialTicks) / 5.F, 0.05F, 1.F);
                scaleY *= 0.05F;
                scaleZ *= MathHelper.clamp((entity.getTimeToDespawn() - partialTicks) / 5.F, 0.05F, 1.F);
                translateY = 0.5F;
            }
        }
        GlStateManager.translate(0, translateY, 0);
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.rotate(-entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.model.render(entity, partialTicks, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    public static void setupTranslation(double x, double y, double z) {
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);
    }
}
