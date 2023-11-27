package gregtech.client.particle;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.renderer.fx.LaserBeamRenderer;
import gregtech.client.utils.EffectRenderContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import codechicken.lib.vec.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTLaserBeamParticle extends GTParticle {

    @Nullable
    private final MetaTileEntity mte;
    @Nullable
    private ResourceLocation body;
    @Nullable
    private ResourceLocation head;
    private Vector3 direction;
    private float beamHeight = 0.075f;
    private float headWidth;
    private float alpha = 1;
    private float emit;
    private boolean doubleVertical;

    public GTLaserBeamParticle(@Nullable MetaTileEntity mte, @NotNull Vector3 startPos, @NotNull Vector3 endPos) {
        super(startPos.x, startPos.y, startPos.z);
        this.mte = mte;
        this.direction = endPos.copy().subtract(startPos);
        this.setRenderRange(64);
    }

    @Override
    public boolean shouldRender(@NotNull EffectRenderContext context) {
        double renderRange = getSquaredRenderRange();
        if (renderRange < 0) return true;
        Vec3d eyePos = context.renderViewEntity().getPositionEyes(context.partialTicks());
        return eyePos.squareDistanceTo(posX, posY, posZ) <= renderRange ||
                eyePos.squareDistanceTo(posX + direction.x, posY + direction.y, posZ + direction.z) <= renderRange;
    }

    /**
     * Set beam body texture
     *
     * @param body texture resource.
     */
    public GTLaserBeamParticle setBody(@Nullable ResourceLocation body) {
        this.body = body;
        return this;
    }

    /**
     * Set head body texture
     *
     * @param head texture resource.
     */
    public GTLaserBeamParticle setHead(@Nullable ResourceLocation head) {
        this.head = head;
        return this;
    }

    public GTLaserBeamParticle setStartPos(@NotNull Vector3 startPos) {
        this.direction.add(posX, posY, posZ).subtract(startPos);
        return this;
    }

    public GTLaserBeamParticle setEndPos(@NotNull Vector3 endPos) {
        this.direction = endPos.copy().subtract(posX, posY, posZ);
        return this;
    }

    public GTLaserBeamParticle setBeamHeight(float beamHeight) {
        this.beamHeight = beamHeight;
        return this;
    }

    public GTLaserBeamParticle setHeadWidth(float headWidth) {
        this.headWidth = headWidth;
        return this;
    }

    public GTLaserBeamParticle setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public float getAlpha() {
        return this.alpha;
    }

    /**
     * Set emit speed.
     *
     * @param emit emit speed. from start to end.
     */
    public GTLaserBeamParticle setEmit(float emit) {
        this.emit = emit;
        return this;
    }

    /**
     * Is 3D beam rendered by two perpendicular quads.
     * <p>
     * It is not about performance, some textures are suitable for this, some are not, please choose according to the
     * texture used.
     * </p>
     */
    public GTLaserBeamParticle setDoubleVertical(boolean doubleVertical) {
        this.doubleVertical = doubleVertical;
        return this;
    }

    @Override
    public boolean shouldDisableDepth() {
        return true;
    }

    @Override
    public void onUpdate() {
        if (mte == null || mte.isValid() &&
                mte.getWorld().isBlockLoaded(mte.getPos(), false) &&
                mte.getWorld().getTileEntity(mte.getPos()) == mte.getHolder()) {
            return;
        }
        setExpired();
    }

    @Override
    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
        GlStateManager.translate(posX - context.cameraX(), posY - context.cameraY(), posZ - context.cameraZ());

        Vector3 cameraDirection = null;
        if (!doubleVertical) {
            Vec3d positionEyes = context.renderViewEntity().getPositionEyes(context.partialTicks());
            cameraDirection = new Vector3(posX, posY, posZ).subtract(new Vector3(positionEyes));
        }
        TextureManager renderEngine = Minecraft.getMinecraft().getRenderManager().renderEngine;
        ITextureObject bodyTexture = null;
        if (body != null) {
            bodyTexture = renderEngine.getTexture(body);
            // noinspection ConstantValue
            if (bodyTexture == null) {
                bodyTexture = new SimpleTexture(body);
                renderEngine.loadTexture(body, bodyTexture);
            }
        }
        ITextureObject headTexture = null;
        if (head != null) {
            headTexture = renderEngine.getTexture(head);
            // noinspection ConstantValue
            if (headTexture == null) {
                headTexture = new SimpleTexture(head);
                renderEngine.loadTexture(head, headTexture);
            }
        }
        float offset = -emit * (Minecraft.getMinecraft().player.ticksExisted + context.partialTicks());
        LaserBeamRenderer.renderRawBeam(bodyTexture == null ? -1 :
                bodyTexture.getGlTextureId(),
                headTexture == null ? -1 :
                        headTexture.getGlTextureId(),
                direction, cameraDirection, beamHeight, headWidth, alpha, offset);
        GlStateManager.translate(context.cameraX() - posX, context.cameraY() - posY, context.cameraZ() - posZ);
    }

    @Nullable
    @Override
    public IRenderSetup getRenderSetup() {
        return SETUP;
    }

    @Override
    public String toString() {
        return "GTLaserBeamParticle{" +
                "mte=" + mte +
                ", body=" + body +
                ", head=" + head +
                ", direction=" + direction +
                ", beamHeight=" + beamHeight +
                ", headWidth=" + headWidth +
                ", alpha=" + alpha +
                ", emit=" + emit +
                ", doubleVertical=" + doubleVertical +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                '}';
    }

    private static final IRenderSetup SETUP = new IRenderSetup() {

        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        public void preDraw(@NotNull BufferBuilder buffer) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableCull();
        }

        @Override
        public void postDraw(@NotNull BufferBuilder buffer) {
            GlStateManager.enableCull();
            GlStateManager.disableRescaleNormal();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
    };
}
