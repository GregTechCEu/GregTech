package gregtech.client.particle;

import gregtech.client.renderer.IRenderSetup;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A custom particle implementation with framework for more advanced rendering capabilities.<p/>
 * GTParticle instances are managed by {@link GTParticleManager}. GTParticle instances with same {@link IRenderSetup}s
 * will be drawn together as a batch.
 */
@SideOnly(Side.CLIENT)
public abstract class GTParticle {

    public double posX;
    public double posY;
    public double posZ;

    private double renderRange = -1;
    private double squaredRenderRange = -1;

    private boolean expired;

    protected GTParticle(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public boolean shouldRender(@Nonnull Entity renderViewEntity, float partialTicks) {
        if (squaredRenderRange < 0) return true;
        return renderViewEntity.getPositionEyes(partialTicks).squareDistanceTo(posX, posY, posZ) <= squaredRenderRange;
    }

    public final boolean isAlive() {
        return !expired;
    }

    public final boolean isExpired() {
        return expired;
    }

    public final void setExpired() {
        this.expired = true;
    }

    /**
     * @return {@code true} to render the particle with
     * {@link net.minecraft.client.renderer.GlStateManager#depthMask(boolean) depth mask} feature disabled; in other
     * words, render the particle without modifying depth buffer.
     */
    public boolean shouldDisableDepth() {
        return false;
    }

    /**
     * @return render range. If the distance between particle and render view entity exceeds this value, the particle
     * will not be rendered. If render range is negative value or {@code NaN}, then the check is disabled and the
     * particle will be rendered regardless of the distance.
     */
    public final double getRenderRange() {
        return this.renderRange;
    }

    /**
     * @return squared render range, or negative value if render distance check is disabled.
     */
    public final double getSquaredRenderRange() {
        return this.squaredRenderRange;
    }

    /**
     * Sets the render range. If the distance between particle and render view entity exceeds this value, the particle
     * will not be rendered. If render range is negative value or {@code NaN}, then the check is disabled and the
     * particle will be rendered regardless of the distance.
     *
     * @param renderRange Render range
     */
    public final void setRenderRange(double renderRange) {
        this.renderRange = renderRange;
        if (renderRange >= 0) this.squaredRenderRange = renderRange * renderRange;
        else this.squaredRenderRange = -1;
    }

    /**
     * Update the particle. This method is called each tick.
     */
    public void onUpdate() {}

    /**
     * Render the particle. If this particle has non-null {@link #getRenderSetup()} associated, this method will be
     * called between a {@link IRenderSetup#preDraw(BufferBuilder)} call and a
     * {@link IRenderSetup#postDraw(BufferBuilder)} call.
     *
     * @param buffer           Buffer builder
     * @param renderViewEntity Render view entity
     * @param partialTicks     Partial ticks
     * @param cameraX          X position of the camera (interpolated X position of the render view entity)
     * @param cameraY          Y position of the camera (interpolated Y position of the render view entity)
     * @param cameraZ          Z position of the camera (interpolated Z position of the render view entity)
     * @param cameraViewDir    View direction of the camera
     * @param rotationX        X rotation of the render view entity
     * @param rotationZ        Z rotation of the render view entity
     * @param rotationYZ       YZ rotation of the render view entity
     * @param rotationXY       XY rotation of the render view entity
     * @param rotationXZ       XZ rotation of the render view entity
     */
    public void renderParticle(@Nonnull BufferBuilder buffer, @Nonnull Entity renderViewEntity,
                               float partialTicks, double cameraX, double cameraY, double cameraZ,
                               @Nonnull Vec3d cameraViewDir, float rotationX, float rotationZ,
                               float rotationYZ, float rotationXY, float rotationXZ) {}

    /**
     * @return Render setup for this particle, if exists
     */
    @Nullable
    public IRenderSetup getRenderSetup() {
        return null;
    }
}
