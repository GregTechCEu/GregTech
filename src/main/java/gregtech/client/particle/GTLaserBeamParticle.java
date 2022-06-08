package gregtech.client.particle;

import codechicken.lib.vec.Vector3;
import gregtech.client.renderer.fx.LaserBeamRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GTLaserBeamParticle extends GTParticle{
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    private ResourceLocation body;
    private ResourceLocation head;
    private Vector3 direction;
    private float beamHeight = 0.075f;
    private float headWidth;
    private float alpha = 1;
    private float emit;
    private boolean doubleVertical;

    public GTLaserBeamParticle(World worldIn, Vector3 startPos, Vector3 endPos) {
        super(worldIn, startPos.x, startPos.y, startPos.z);
        this.setMotionless(true);
        this.setImmortal();
        this.setRenderRange(64);
        this.direction = endPos.copy().subtract(startPos);
    }

    @Override
    public boolean shouldRendered(Entity entityIn, float partialTicks) {
        if (squareRenderRange < 0) return true;
        Vec3d eyePos = entityIn.getPositionEyes(partialTicks);
        return eyePos.squareDistanceTo(posX, posY, posZ) <= squareRenderRange ||
                eyePos.squareDistanceTo(posX + direction.x, posY + direction.y, posZ + direction.z) <= squareRenderRange;
    }

    /**
     * Set beam body texture
     * 
     * @param body texture resource.
     */
    public GTLaserBeamParticle setBody(ResourceLocation body) {
        this.body = body;
        return this;
    }

    /**
     * Set head body texture
     * 
     * @param head texture resource.
     */
    public GTLaserBeamParticle setHead(ResourceLocation head) {
        this.head = head;
        return this;
    }

    public GTLaserBeamParticle setStartPos(Vector3 startPos) {
        this.direction.add(posX, posY, posZ).subtract(startPos);
        return this;
    }

    public GTLaserBeamParticle setEndPos(Vector3 endPos) {
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
     * @param emit emit speed. from start to end.
     */
    public GTLaserBeamParticle setEmit(float emit) {
        this.emit = emit;
        return this;
    }

    /**
     * Is 3D beam rendered by two perpendicular quads.
     * <P>
     *     It is not about performance, some textures are suitable for this, some are not, please choose according to the texture used.
     * </P>
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
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        GlStateManager.translate(posX - interpPosX, posY - interpPosY, posZ - interpPosZ);

        Vector3 cameraDirection = null;
        if (!doubleVertical) {
            cameraDirection = new Vector3(posX, posY, posZ).subtract(new Vector3(entityIn.getPositionEyes(partialTicks)));
        }
        TextureManager renderEngine = MINECRAFT.getRenderManager().renderEngine;
        ITextureObject bodyTexture = null;
        if (body != null) {
            bodyTexture = renderEngine.getTexture(body);
            if (bodyTexture == null) {
                bodyTexture = new SimpleTexture(body);
                renderEngine.loadTexture(body, bodyTexture);
            }
        }
        ITextureObject headTexture = null;
        if (head != null) {
            headTexture = renderEngine.getTexture(head);
            if (headTexture == null) {
                headTexture = new SimpleTexture(head);
                renderEngine.loadTexture(head, headTexture);
            }
        }
        float offset = - emit * (MINECRAFT.player.ticksExisted + partialTicks);
        LaserBeamRenderer.renderRawBeam(bodyTexture == null ? -1 : bodyTexture.getGlTextureId(), headTexture == null ? -1 : headTexture.getGlTextureId(), direction, cameraDirection, beamHeight, headWidth, alpha, offset);
        GlStateManager.translate(interpPosX - posX, interpPosY - posY, interpPosZ - posZ);
    }

    @Override
    public IGTParticleHandler getGLHandler() {
        return HANDLER;
    }

    public static IGTParticleHandler HANDLER = new IGTParticleHandler() {
        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        public void preDraw(BufferBuilder buffer) {
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableCull();
        }

        @Override
        public void postDraw(BufferBuilder buffer) {
            GlStateManager.enableCull();
            GlStateManager.disableRescaleNormal();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }

    };
}
