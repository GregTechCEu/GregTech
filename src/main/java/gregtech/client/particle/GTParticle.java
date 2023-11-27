package gregtech.client.particle;

import gregtech.client.renderer.IRenderSetup;
import gregtech.client.utils.EffectRenderContext;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom particle implementation with framework for more advanced rendering capabilities.
 * <p/>
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

    public boolean shouldRender(@NotNull EffectRenderContext context) {
        if (squaredRenderRange < 0) return true;
        return context.renderViewEntity().getPositionEyes(context.partialTicks())
                .squareDistanceTo(posX, posY, posZ) <= squaredRenderRange;
    }

    public final boolean isAlive() {
        return !expired;
    }

    public final boolean isExpired() {
        return expired;
    }

    public final void setExpired() {
        if (this.expired) return;
        this.expired = true;
        onExpired();
    }

    /**
     * @return {@code true} to render the particle with
     *         {@link net.minecraft.client.renderer.GlStateManager#depthMask(boolean) depth mask} feature disabled; in
     *         other
     *         words, render the particle without modifying depth buffer.
     */
    public boolean shouldDisableDepth() {
        return false;
    }

    /**
     * @return render range. If the distance between particle and render view entity exceeds this value, the particle
     *         will not be rendered. If render range is negative value or {@code NaN}, then the check is disabled and
     *         the
     *         particle will be rendered regardless of the distance.
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
     * Called once on expiration.
     */
    protected void onExpired() {}

    /**
     * Render the particle. If this particle has non-null {@link #getRenderSetup()} associated, this method will be
     * called between a {@link IRenderSetup#preDraw(BufferBuilder)} call and a
     * {@link IRenderSetup#postDraw(BufferBuilder)} call.
     *
     * @param buffer  buffer builder
     * @param context render context
     */
    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {}

    /**
     * @return Render setup for this particle, if exists
     */
    @Nullable
    public IRenderSetup getRenderSetup() {
        return null;
    }
}
