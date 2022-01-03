package gregtech.client.particle;

import codechicken.lib.vec.Vector3;
import gregtech.client.renderer.fx.LaserBeamRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GTLaserBeamParticle extends GTParticle{
    private ResourceLocation body;
    private ResourceLocation head;
    private Vector3 direction;
    private float beamHeight = 0.075f;
    private float headWidth = 0;
    private float alpha = 1;
    private float emit = 0;
    private boolean doubleVertical = true;

    public GTLaserBeamParticle(World worldIn, Vector3 startPos, Vector3 endPos) {
        super(worldIn, startPos.x, startPos.y, startPos.z);
        this.setMotionless(true);
        this.setImmortal();
        this.direction = endPos.copy().subtract(startPos);
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

    public GTLaserBeamParticle setBeamHeigth(float beamHeight) {
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
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        GlStateManager.translate(posX - interpPosX, posY - interpPosY, posZ - interpPosZ);

        Vector3 cameraDir = null;
        if (!doubleVertical) {
            cameraDir = new Vector3(posX, posY, posZ).subtract(new Vector3(entityIn.getPositionEyes(partialTicks)));
        }
        LaserBeamRenderer.renderBeam(body, head, direction, cameraDir, beamHeight, headWidth, 1, emit);

        GlStateManager.translate(interpPosX - posX, interpPosY - posY, interpPosZ - posZ);
    }

    @Override
    public IGTParticleHandler getGLHandler() {
        return HANDLER;
    }

    public static IGTParticleHandler HANDLER = new IGTParticleHandler() {
        int lastBlendFuncSrc;
        int lastBlendFuncDest;
        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        public void preDraw(BufferBuilder buffer) {
            lastBlendFuncSrc = GlStateManager.glGetInteger(GL11.GL_BLEND_SRC);
            lastBlendFuncDest = GlStateManager.glGetInteger(GL11.GL_BLEND_DST);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableCull();
            GlStateManager.depthMask(false);
        }

        @Override
        public void postDraw(BufferBuilder buffer) {
            GlStateManager.depthMask(true);
            GlStateManager.enableCull();
            GlStateManager.disableRescaleNormal();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
            GlStateManager.blendFunc(lastBlendFuncSrc, lastBlendFuncDest);
            GlStateManager.disableBlend();
        }

    };
}
