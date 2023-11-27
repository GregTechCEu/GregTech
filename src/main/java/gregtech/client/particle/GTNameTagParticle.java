package gregtech.client.particle;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.client.utils.EffectRenderContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GTNameTagParticle extends GTParticle {

    private final MetaTileEntityHolder metaTileEntityHolder;

    public GTNameTagParticle(@NotNull MetaTileEntityHolder metaTileEntityHolder, double posX, double posY,
                             double posZ) {
        super(posX, posY, posZ);
        this.metaTileEntityHolder = Objects.requireNonNull(metaTileEntityHolder);
        this.setRenderRange(64);
    }

    @Override
    public void onUpdate() {
        if (metaTileEntityHolder.isInvalid() ||
                !metaTileEntityHolder.getWorld().isBlockLoaded(metaTileEntityHolder.getPos(), false) ||
                !metaTileEntityHolder.hasCustomName()) {
            setExpired();
        }
    }

    @Override
    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
        String name = this.metaTileEntityHolder.getName();
        if (name.isEmpty()) return;

        Entity renderViewEntity = context.renderViewEntity();
        float rotationYaw = renderViewEntity.prevRotationYaw +
                (renderViewEntity.rotationYaw - renderViewEntity.prevRotationYaw) * context.partialTicks();
        float rotationPitch = renderViewEntity.prevRotationPitch +
                (renderViewEntity.rotationPitch - renderViewEntity.prevRotationPitch) * context.partialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX - context.cameraX(), posY - context.cameraY(), posZ - context.cameraZ());
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rotationPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.depthMask(false);

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(name) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-width - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos(-width - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos(width + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos(width + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        Minecraft.getMinecraft().fontRenderer.drawString(name, -width, 0, -1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public String toString() {
        return "GTNameTagParticle{" +
                "metaTileEntityHolder=" + metaTileEntityHolder +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                '}';
    }
}
