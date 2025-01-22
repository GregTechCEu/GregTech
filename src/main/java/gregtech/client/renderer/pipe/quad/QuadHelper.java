package gregtech.client.renderer.pipe.quad;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
public final class QuadHelper {

    private static final FaceBakery BAKERY = new FaceBakery();

    private QuadHelper() {}

    public static @NotNull RecolorableBakedQuad buildQuad(EnumFacing normal, Pair<Vector3f, Vector3f> box,
                                                          @NotNull UVMapper uv,
                                                          @NotNull SpriteInformation targetSprite) {
        BlockPartFace face = new BlockPartFace(null, -1, targetSprite.sprite().getIconName(), uv.map(normal, box));
        BakedQuad quad = BAKERY.makeBakedQuad(box.getLeft(), box.getRight(), face, targetSprite.sprite(), normal,
                ModelRotation.X0_Y0, null, false, true);
        return new RecolorableBakedQuad(quad, targetSprite);
    }

    public static @NotNull BakedQuad buildQuad(EnumFacing normal, Pair<Vector3f, Vector3f> box,
                                               @NotNull UVMapper uv, @NotNull TextureAtlasSprite targetSprite) {
        BlockPartFace face = new BlockPartFace(null, -1, targetSprite.getIconName(), uv.map(normal, box));
        return BAKERY.makeBakedQuad(box.getLeft(), box.getRight(), face, targetSprite, normal, ModelRotation.X0_Y0,
                null, false, true);
    }

    @Contract("_ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> toPair(@NotNull AxisAlignedBB bb) {
        return ImmutablePair.of(new Vector3f((float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16),
                new Vector3f((float) bb.maxX * 16, (float) bb.maxY * 16, (float) bb.maxZ * 16));
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> toPair(float x1, float y1, float z1, float x2, float y2,
                                                                    float z2) {
        return ImmutablePair.of(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> capOverlay(@Nullable EnumFacing facing,
                                                                        @NotNull AxisAlignedBB bb, float g) {
        return capOverlay(facing, (float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16,
                (float) bb.maxX * 16, (float) bb.maxY * 16,
                (float) bb.maxZ * 16, g);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> capOverlay(@Nullable EnumFacing facing, float x1, float y1,
                                                                        float z1, float x2, float y2, float z2,
                                                                        float g) {
        if (facing == null) return toPair(x1 - g, y1 - g, z1 - g, x2 + g, y2 + g, z2 + g);
        return switch (facing.getAxis()) {
            case X -> toPair(x1 - g, y1, z1, x2 + g, y2, z2);
            case Y -> toPair(x1, y1 - g, z1, x2, y2 + g, z2);
            case Z -> toPair(x1, y1, z1 - g, x2, y2, z2 + g);
        };
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> tubeOverlay(@Nullable EnumFacing facing,
                                                                         @NotNull AxisAlignedBB bb, float g) {
        return tubeOverlay(facing, (float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16,
                (float) bb.maxX * 16, (float) bb.maxY * 16,
                (float) bb.maxZ * 16, g);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> tubeOverlay(@Nullable EnumFacing facing, float x1,
                                                                         float y1, float z1, float x2, float y2,
                                                                         float z2, float g) {
        if (facing == null) return toPair(x1, y1, z1, x2, y2, z2);
        return switch (facing.getAxis()) {
            case X -> toPair(x1, y1 - g, z1 - g, x2, y2 + g, z2 + g);
            case Y -> toPair(x1 - g, y1, z1 - g, x2 + g, y2, z2 + g);
            case Z -> toPair(x1 - g, y1 - g, z1, x2 + g, y2 + g, z2);
        };
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> fullOverlay(@Nullable EnumFacing facing,
                                                                         @NotNull AxisAlignedBB bb, float g) {
        return fullOverlay(facing, (float) bb.minX * 16, (float) bb.minY * 16, (float) bb.minZ * 16,
                (float) bb.maxX * 16, (float) bb.maxY * 16,
                (float) bb.maxZ * 16, g);
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull ImmutablePair<Vector3f, Vector3f> fullOverlay(@Nullable EnumFacing facing, float x1,
                                                                         float y1, float z1, float x2, float y2,
                                                                         float z2, float g) {
        return toPair(x1 - g, y1 - g, z1 - g, x2 + g, y2 + g, z2 + g);
    }
}
