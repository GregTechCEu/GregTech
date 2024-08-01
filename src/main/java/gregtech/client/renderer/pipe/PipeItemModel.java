package gregtech.client.renderer.pipe;

import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.util.CacheKey;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.EnumMap;
import java.util.List;

public class PipeItemModel<K extends CacheKey> implements IBakedModel {

    private static final EnumMap<ItemCameraTransforms.TransformType, Matrix4f> CAMERA_TRANSFORMS =
            new EnumMap<>(ItemCameraTransforms.TransformType.class);

    static {
        // TODO these transforms are atrocious
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.NONE, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.rotY((float) Math.toRadians(225));
        matrix4f.rotX((float) Math.toRadians(30));
        matrix4f.setScale(0.625f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.GUI, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.setTranslation(new Vector3f(0, 3, 0));
        matrix4f.setScale(0.25f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.GROUND, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.setScale(0.5f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.FIXED, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.rotY((float) Math.toRadians(45));
        matrix4f.rotX((float) Math.toRadians(75));
        matrix4f.setTranslation(new Vector3f(0, 2.5f, 0));
        matrix4f.setScale(0.375f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.rotY((float) Math.toRadians(45));
        matrix4f.rotX((float) Math.toRadians(75));
        matrix4f.setTranslation(new Vector3f(0, 2.5f, 0));
        matrix4f.setScale(0.375f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.rotY((float) Math.toRadians(45));
        matrix4f.setScale(0.4f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, matrix4f);
        matrix4f = new Matrix4f();
        matrix4f.rotY((float) Math.toRadians(225));
        matrix4f.setScale(0.4f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, matrix4f);
    }

    private final AbstractPipeModel<K> basis;
    private final K key;
    private final ColorData data;

    public PipeItemModel(AbstractPipeModel<K> basis, K key, ColorData data) {
        this.basis = basis;
        this.key = key;
        this.data = data;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return basis.getQuads(key, (byte) 0b1100, (byte) 0b0, (byte) 0b0, data, null, (byte) 0b0);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return basis.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return basis.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @NotNull Pair<? extends IBakedModel, Matrix4f> handlePerspective(
            ItemCameraTransforms.@NotNull TransformType cameraTransformType) {
        return ImmutablePair.of(this, CAMERA_TRANSFORMS.get(cameraTransformType));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return basis.getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
