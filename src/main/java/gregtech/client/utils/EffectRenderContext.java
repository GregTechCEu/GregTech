package gregtech.client.utils;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Collection of various information for rendering purposes.
 */
public final class EffectRenderContext {

    private static final EffectRenderContext instance = new EffectRenderContext();

    public static EffectRenderContext getInstance() {
        return instance;
    }

    @Nullable
    private Entity renderViewEntity;
    private float partialTicks;
    private double cameraX;
    private double cameraY;
    private double cameraZ;
    @NotNull
    private Vec3d cameraViewDir = Vec3d.ZERO;
    private float rotationX;
    private float rotationZ;
    private float rotationYZ;
    private float rotationXY;
    private float rotationXZ;

    @NotNull
    public EffectRenderContext update(@NotNull Entity renderViewEntity, float partialTicks) {
        this.renderViewEntity = renderViewEntity;
        this.partialTicks = partialTicks;

        this.cameraX = renderViewEntity.lastTickPosX +
                (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
        this.cameraY = renderViewEntity.lastTickPosY +
                (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
        this.cameraZ = renderViewEntity.lastTickPosZ +
                (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
        this.cameraViewDir = renderViewEntity.getLook(partialTicks);

        this.rotationX = ActiveRenderInfo.getRotationX();
        this.rotationZ = ActiveRenderInfo.getRotationZ();
        this.rotationYZ = ActiveRenderInfo.getRotationYZ();
        this.rotationXY = ActiveRenderInfo.getRotationXY();
        this.rotationXZ = ActiveRenderInfo.getRotationXZ();

        return this;
    }

    /**
     * @return render view entity
     */
    @NotNull
    public Entity renderViewEntity() {
        return Objects.requireNonNull(renderViewEntity, "renderViewEntity not available yet");
    }

    /**
     * @return partial ticks
     */
    public float partialTicks() {
        return partialTicks;
    }

    /**
     * @return X position of the camera (interpolated X position of the render view entity)
     */
    public double cameraX() {
        return cameraX;
    }

    /**
     * @return Y position of the camera (interpolated Y position of the render view entity)
     */
    public double cameraY() {
        return cameraY;
    }

    /**
     * @return Z position of the camera (interpolated Z position of the render view entity)
     */
    public double cameraZ() {
        return cameraZ;
    }

    /**
     * @return view direction of the camera
     */
    @NotNull
    public Vec3d cameraViewDir() {
        return cameraViewDir;
    }

    /**
     * @return X rotation of the render view entity
     */
    public float rotationX() {
        return rotationX;
    }

    /**
     * @return Z rotation of the render view entity
     */
    public float rotationZ() {
        return rotationZ;
    }

    /**
     * @return YZ rotation of the render view entity
     */
    public float rotationYZ() {
        return rotationYZ;
    }

    /**
     * @return XY rotation of the render view entity
     */
    public float rotationXY() {
        return rotationXY;
    }

    /**
     * @return XZ rotation of the render view entity
     */
    public float rotationXZ() {
        return rotationXZ;
    }
}
