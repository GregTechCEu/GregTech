package gregtech.client.renderer.pipe.cover;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import javax.vecmath.Vector3f;

@SideOnly(Side.CLIENT)
public class CoverRendererBuilder {

    private static final ColorQuadCache[] PLATE_QUADS;
    private static final EnumMap<EnumFacing, SubListAddress> PLATE_COORDS = new EnumMap<>(EnumFacing.class);

    private static final UVMapper DEFAULT_MAPPER = UVMapper.standard(0);

    static {
        PLATE_QUADS = new ColorQuadCache[Textures.VOLTAGE_CASINGS.length];
        for (int i = 0; i < Textures.VOLTAGE_CASINGS.length; i++) {
            if (Textures.VOLTAGE_CASINGS[i] == null) break;
            PLATE_QUADS[i] = buildPlates(new SpriteInformation(plateSprite(i), 0));
        }
    }

    private static @NotNull TextureAtlasSprite plateSprite(int i) {
        return Textures.VOLTAGE_CASINGS[i].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
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
        Pair<Vector3f, Vector3f> box = CoverRendererValues.PLATE_BOXES.get(facing);
        for (EnumFacing dir : EnumFacing.values()) {
            quads.add(QuadHelper.buildQuad(dir, box, CoverRendererBuilder.DEFAULT_MAPPER, sprite));
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

    protected UVMapper mapper = DEFAULT_MAPPER;
    protected UVMapper mapperEmissive = DEFAULT_MAPPER;

    protected ColorQuadCache plateQuads = PLATE_QUADS[1];

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

    public CoverRendererBuilder setPlateQuads(int index) {
        this.plateQuads = PLATE_QUADS[index];
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
                    QuadHelper.buildQuad(facing, CoverRendererValues.OVERLAY_BOXES_1.get(facing), mapper, sprite),
                    QuadHelper.buildQuad(facing.getOpposite(), CoverRendererValues.OVERLAY_BOXES_1.get(facing), mapper,
                            sprite)));
            if (spriteEmissive != null) spriteEmissiveQuads.put(facing, ImmutablePair.of(
                    QuadHelper.buildQuad(facing, CoverRendererValues.OVERLAY_BOXES_2.get(facing), mapperEmissive,
                            spriteEmissive),
                    QuadHelper.buildQuad(facing.getOpposite(), CoverRendererValues.OVERLAY_BOXES_2.get(facing),
                            mapperEmissive,
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
