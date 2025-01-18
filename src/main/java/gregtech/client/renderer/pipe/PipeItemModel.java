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
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

@SideOnly(Side.CLIENT)
public class PipeItemModel<K extends CacheKey> implements IBakedModel {

    private static final EnumMap<ItemCameraTransforms.TransformType, Matrix4f> CAMERA_TRANSFORMS = new EnumMap<>(
            ItemCameraTransforms.TransformType.class);

    static {
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.NONE, TRSRTransformation.mul(null, null, null, null));
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.GUI,
                TRSRTransformation.mul(null, rotDegrees(30, -45, 0), scale(0.625f), null));
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.GROUND,
                TRSRTransformation.mul(null, null, scale(0.25f), null));
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.FIXED,
                TRSRTransformation.mul(null, rotDegrees(0, 90, 0), scale(0.5f), null));
        Matrix4f matrix4f = TRSRTransformation.mul(null, rotDegrees(75, 45, 0), scale(0.375f), null);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, matrix4f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, matrix4f);
        matrix4f = TRSRTransformation.mul(null, rotDegrees(0, 45, 0), scale(0.4f), null);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, matrix4f);
        CAMERA_TRANSFORMS.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, matrix4f);
    }

    private static Vector3f scale(float scale) {
        return new Vector3f(scale, scale, scale);
    }

    private static Quat4f rotDegrees(float x, float y, float z) {
        return TRSRTransformation.quatFromXYZDegrees(new Vector3f(x, y, z));
    }

    private final PipeModelRedirector redirector;
    private final AbstractPipeModel<K> basis;
    private final K key;
    private final ColorData data;

    public PipeItemModel(PipeModelRedirector redirector, AbstractPipeModel<K> basis, K key, ColorData data) {
        this.redirector = redirector;
        this.basis = basis;
        this.key = key;
        this.data = data;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        byte z = 0;
        return basis.getQuads(key, (byte) 0b1100, z, z, data, null, z, z);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return redirector.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return redirector.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @NotNull Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.@NotNull TransformType cameraTransformType) {
        return ImmutablePair.of(this, CAMERA_TRANSFORMS.get(cameraTransformType));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return redirector.getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
