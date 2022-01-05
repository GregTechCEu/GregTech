package gregtech.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description:
 */
@SideOnly(Side.CLIENT)
public abstract class GTParticle extends Particle {
    protected int texturesCount = 1;
    protected int squareRenderRange = -1;
    protected boolean motionless = false;
    protected Consumer<GTParticle> onUpdate;

    public GTParticle(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }

    public GTParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }

    @Override
    public int getFXLayer() {
        return shouldDisableDepth() ? 1 : 0;
    }

    public boolean shouldRendered(Entity entityIn, float partialTicks) {
        if (squareRenderRange < 0) return true;
        return entityIn.getPositionEyes(partialTicks).squareDistanceTo(posX, posY, posZ) <= squareRenderRange;
    }

    /**
     * Set the render range, over the range do not render.
     * <P>
     *     -1 -- always render.
     * </P>
     */
    public void setRenderRange(int renderRange) {
        this.squareRenderRange = renderRange * renderRange;
    }

    /**
     * Particles can live forever now.
     */
    public void setImmortal() {
        this.particleAge = -1;
    }

    /**
     * It can stop motion. It always has a motion before {@link Particle#onUpdate()}
     */
    public void setMotionless(boolean motionless) {
        this.motionless = motionless;
    }

    /**
     * Set color blend of this particle.
     */
    public void setColor(int color) {
        this.setRBGColorF((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F);
        this.setAlphaF((color >> 24 & 255) / 255.0F);
    }

    /**
     * Set scale of this particle.
     */
    public void setScale(float scale) {
        this.particleScale = scale;
    }

    /**
     * Set Gravity of this particle.
     */
    public void setGravity(float gravity) {
        this.particleGravity = gravity;
    }

    /**
     * Set sub-texture coord.
     */
    public void setTexturesIndex(int particleTextureIndexX, int particleTextureIndexY) {
        this.particleTextureIndexX = particleTextureIndexX;
        this.particleTextureIndexY = particleTextureIndexY;
    }

    /**
     * How many sub-textures in the current texture. it always 16 in the {@link Particle}. but we allow the bigger or smaller texture in the GTParticle.
     */
    public void setTexturesCount(int texturesCount) {
        this.texturesCount = texturesCount;
    }

    /**
     * Update each tick
     */
    public void setOnUpdate(Consumer<GTParticle> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setParticleTextureIndex(int particleTextureIndex) {
        this.particleTextureIndexX = particleTextureIndex % texturesCount;
        this.particleTextureIndexY = particleTextureIndex / texturesCount;
    }

    public float getTexturesCount() {
        return texturesCount;
    }

    public boolean isMotionless() {
        return motionless;
    }

    public int getRenderRange() {
        return squareRenderRange >= 0 ? -1 : (int) Math.sqrt(squareRenderRange);
    }

    @Override
    public void onUpdate() {
        if (this.onUpdate != null) {
            onUpdate.accept(this);
        }
        if (this.particleAge >= 0 && this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        if (!motionless) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            this.motionY -= 0.04D * (double)this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;

            if (this.onGround) {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float minU = this.particleTextureIndexX * 1F / texturesCount;
        float maxU = minU + 1F / texturesCount;//0.0624375F;
        float minV = this.particleTextureIndexY * 1F / texturesCount;
        float maxV = minV + 1F / texturesCount;//0.0624375F;
        float scale = 0.1F * this.particleScale;

        if (this.particleTexture != null) {
            minU = this.particleTexture.getMinU();
            maxU = this.particleTexture.getMaxU();
            minV = this.particleTexture.getMinV();
            maxV = this.particleTexture.getMaxV();
        }

        float renderX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float renderY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float renderZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int brightnessForRender = this.getBrightnessForRender(partialTicks);
        int j = brightnessForRender >> 16 & 65535;
        int k = brightnessForRender & 65535;
        buffer.pos(renderX - rotationX * scale - rotationXY * scale, renderY - rotationZ * scale,  (renderZ - rotationYZ * scale - rotationXZ * scale)).tex(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX - rotationX * scale + rotationXY * scale, renderY + rotationZ * scale,  (renderZ - rotationYZ * scale + rotationXZ * scale)).tex(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX + rotationX * scale + rotationXY * scale,  (renderY + rotationZ * scale),  (renderZ + rotationYZ * scale + rotationXZ * scale)).tex(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX + rotationX * scale - rotationXY * scale,  (renderY - rotationZ * scale),  (renderZ + rotationYZ * scale - rotationXZ * scale)).tex(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }

    /***
     * Do not create an instance here; use a static instance plz
     */
    public IGTParticleHandler getGLHandler() {
        return IGTParticleHandler.DEFAULT_FX_HANDLER;
    }
}
