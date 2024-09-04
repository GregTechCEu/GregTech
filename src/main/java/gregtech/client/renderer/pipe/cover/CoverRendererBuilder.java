package gregtech.client.renderer.pipe.cover;

import gregtech.api.GTValues;
import gregtech.api.cover.CoverUtil;
import gregtech.client.renderer.pipe.cache.ColorQuadCache;
import gregtech.client.renderer.pipe.cache.SubListAddress;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.QuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;
import gregtech.client.renderer.pipe.quad.UVMapper;
import gregtech.client.renderer.pipe.util.SpriteInformation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CoverRendererBuilder {

    private static final float OVERLAY_DIST_1 = 0.003f;
    private static final float OVERLAY_DIST_2 = 0.006f;

    private static final ColorQuadCache PLATE_QUADS;
    private static final EnumMap<EnumFacing, SubListAddress> PLATE_COORDS = new EnumMap<>(EnumFacing.class);

    public static final EnumMap<EnumFacing, AxisAlignedBB> PLATE_AABBS = new EnumMap<>(EnumFacing.class);
    private static final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> PLATE_BOXES = new EnumMap<>(EnumFacing.class);
    private static final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> OVERLAY_BOXES_1 = new EnumMap<>(
            EnumFacing.class);
    private static final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> OVERLAY_BOXES_2 = new EnumMap<>(
            EnumFacing.class);

    private static final UVMapper defaultMapper = UVMapper.standard(180);

    static {
        for (EnumFacing facing : EnumFacing.VALUES) {
            PLATE_AABBS.put(facing, CoverUtil.getCoverPlateBox(facing, 1d / 16).aabb());
        }
        for (var value : PLATE_AABBS.entrySet()) {
            // make sure that plates render slightly below any normal block quad
            PLATE_BOXES.put(value.getKey(), QuadHelper.fullOverlay(value.getKey(), value.getValue(), -OVERLAY_DIST_1));
            OVERLAY_BOXES_1.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue(), OVERLAY_DIST_1));
            OVERLAY_BOXES_2.put(value.getKey(),
                    QuadHelper.fullOverlay(value.getKey(), value.getValue(), OVERLAY_DIST_2));
        }
        PLATE_QUADS = buildPlates(new SpriteInformation(defaultPlateSprite(), 0));
    }

    private static @NotNull TextureAtlasSprite defaultPlateSprite() {
        return Textures.VOLTAGE_CASINGS[GTValues.LV].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public static ColorQuadCache buildPlates(SpriteInformation sprite) {
        List<RecolorableBakedQuad> quads = new ObjectArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            PLATE_COORDS.put(facing, buildPlates(quads, facing, sprite));
        }
        return new ColorQuadCache(quads);
    }

    protected static SubListAddress buildPlates(List<RecolorableBakedQuad> quads, EnumFacing facing,
                                                SpriteInformation sprite) {
        int start = quads.size();
        Pair<Vector3f, Vector3f> box = PLATE_BOXES.get(facing);
        for (EnumFacing dir : EnumFacing.values()) {
            quads.add(QuadHelper.buildQuad(dir, box, CoverRendererBuilder.defaultMapper, sprite));
        }
        return new SubListAddress(start, quads.size());
    }

    protected static void addPlates(List<BakedQuad> quads, List<BakedQuad> plateQuads, EnumSet<EnumFacing> plates) {
        for (EnumFacing facing : plates) {
            quads.add(plateQuads.get(facing.ordinal()));
        }
    }

    protected final TextureAtlasSprite sprite;
    protected final TextureAtlasSprite spriteEmissive;

    protected UVMapper mapper = defaultMapper;
    protected UVMapper mapperEmissive = defaultMapper;

    protected ColorQuadCache plateQuads = PLATE_QUADS;

    public CoverRendererBuilder(@NotNull SimpleOverlayRenderer overlay) {
        this(overlay.getSprite(), overlay.getSpriteEmissive());
    }

    public CoverRendererBuilder(@NotNull TextureAtlasSprite sprite, @Nullable TextureAtlasSprite spriteEmissive) {
        this.sprite = sprite;
        this.spriteEmissive = spriteEmissive;
    }

    public CoverRendererBuilder setMapper(@NotNull UVMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public CoverRendererBuilder setMapperEmissive(@NotNull UVMapper mapperEmissive) {
        this.mapperEmissive = mapperEmissive;
        return this;
    }

    public CoverRendererBuilder setPlateQuads(ColorQuadCache cache) {
        this.plateQuads = cache;
        return this;
    }

    protected static List<BakedQuad> getPlates(EnumFacing facing, ColorData data, ColorQuadCache plateQuads) {
        return PLATE_COORDS.get(facing).getSublist(plateQuads.getQuads(data));
    }

    public CoverRenderer build() {
        EnumMap<EnumFacing, Pair<BakedQuad, BakedQuad>> spriteQuads = new EnumMap<>(EnumFacing.class);
        EnumMap<EnumFacing, Pair<BakedQuad, BakedQuad>> spriteEmissiveQuads = spriteEmissive != null ?
                new EnumMap<>(EnumFacing.class) : null;
        for (EnumFacing facing : EnumFacing.VALUES) {
            spriteQuads.put(facing, ImmutablePair.of(
                    QuadHelper.buildQuad(facing, OVERLAY_BOXES_1.get(facing), mapper, sprite),
                    QuadHelper.buildQuad(facing.getOpposite(), OVERLAY_BOXES_1.get(facing), mapper, sprite)));
            if (spriteEmissive != null) spriteEmissiveQuads.put(facing, ImmutablePair.of(
                    QuadHelper.buildQuad(facing, OVERLAY_BOXES_2.get(facing), mapperEmissive, spriteEmissive),
                    QuadHelper.buildQuad(facing.getOpposite(), OVERLAY_BOXES_2.get(facing), mapperEmissive,
                            spriteEmissive)));
        }

        return (quads, facing, renderPlate, renderBackside, renderLayer, data) -> {
            if (renderLayer == BlockRenderLayer.CUTOUT_MIPPED) {
                addPlates(quads, getPlates(facing, data, plateQuads), renderPlate);
                quads.add(spriteQuads.get(facing).getLeft());
                if (renderBackside) quads.add(spriteQuads.get(facing).getRight());
            }
            if (spriteEmissiveQuads != null && renderLayer == BloomEffectUtil.getEffectiveBloomLayer()) {
                quads.add(spriteEmissiveQuads.get(facing).getLeft());
                if (renderBackside) quads.add(spriteEmissiveQuads.get(facing).getRight());
            }
        };
    }
}
