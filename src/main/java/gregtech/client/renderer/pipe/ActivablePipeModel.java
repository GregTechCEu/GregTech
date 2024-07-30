package gregtech.client.renderer.pipe;

import gregtech.api.block.UnlistedBooleanProperty;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cache.ActivableSQC;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.ActivableCacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ActivablePipeModel extends AbstractPipeModel<ActivableCacheKey> {

    private static final ResourceLocation loc = GTUtility.gregtechId("block/pipe_activable");

    public static final UnlistedBooleanProperty ACTIVE_PROPERTY = new UnlistedBooleanProperty("active");

    public static final ActivablePipeModel OPTICAL = new ActivablePipeModel(Textures.OPTICAL_PIPE_IN,
            Textures.OPTICAL_PIPE_SIDE, Textures.OPTICAL_PIPE_SIDE_OVERLAY, Textures.OPTICAL_PIPE_SIDE_OVERLAY_ACTIVE,
            false, "optical");
    public static final ActivablePipeModel LASER = new ActivablePipeModel(Textures.LASER_PIPE_IN,
            Textures.LASER_PIPE_SIDE, Textures.LASER_PIPE_OVERLAY, Textures.LASER_PIPE_OVERLAY_EMISSIVE,
            true, "laser");

    private final Supplier<SpriteInformation> inTex;
    private final Supplier<SpriteInformation> sideTex;
    private final Supplier<SpriteInformation> overlayTex;
    private final Supplier<SpriteInformation> overlayActiveTex;

    private final boolean emissiveActive;

    public ActivablePipeModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex,
                              @NotNull Supplier<SpriteInformation> overlayTex,
                              @NotNull Supplier<SpriteInformation> overlayActiveTex, boolean emissiveActive,
                              String variant) {
        super(new ModelResourceLocation(loc, variant));
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.overlayTex = overlayTex;
        this.overlayActiveTex = overlayActiveTex;
        this.emissiveActive = emissiveActive;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(ActivableCacheKey key, byte connectionMask, byte closedMask,
                                             byte blockedMask, int argb, @Nullable Material frameMaterial,
                                             byte frameMask) {
        boolean bloomLayer = getCurrentRenderLayer() == BloomEffectUtil.getEffectiveBloomLayer();
        // don't render the main shape to the bloom layer
        List<BakedQuad> quads = bloomLayer ? new ObjectArrayList<>() :
                super.getQuads(key, connectionMask, closedMask, blockedMask, argb, frameMaterial, frameMask);

        if (!bloomLayer && (!key.isActive() || !allowActive())) {
            ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, argb, false);
        } else {
            if (emissiveActive && bloomLayer) {
                ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, argb, true);
                // TODO bake this into the original quads
                quads = quads.stream().map(RenderUtil::makeEmissive).collect(Collectors.toList());
            } else if (!emissiveActive && getCurrentRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED) {
                ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, argb, true);
            }
        }
        return quads;
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideTex.get();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return getParticleSprite(null).sprite();
    }

    @Override
    protected @NotNull ActivableCacheKey toKey(@NotNull IExtendedBlockState state) {
        return ActivableCacheKey.of(state.getValue(THICKNESS_PROPERTY), state.getValue(ACTIVE_PROPERTY));
    }

    @Override
    protected StructureQuadCache constructForKey(ActivableCacheKey key) {
        return ActivableSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get(),
                overlayTex.get(), overlayActiveTex.get());
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED ||
                (allowActive() && emissiveActive && layer == BloomEffectUtil.getEffectiveBloomLayer());
    }

    public boolean allowActive() {
        return !ConfigHolder.client.preventAnimatedCables;
    }

    @Override
    protected @Nullable PipeItemModel<ActivableCacheKey> getItemModel(@NotNull ItemStack stack, World world,
                                                                      EntityLivingBase entity) {
        WorldPipeBlock block = WorldPipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        return new PipeItemModel<>(this, new ActivableCacheKey(block.getStructure().getRenderThickness(), false),
                PipeTileEntity.DEFAULT_COLOR);
    }

    public static void registerModels(IRegistry<ModelResourceLocation, IBakedModel> registry) {
        registry.putObject(OPTICAL.getLoc(), OPTICAL);
        registry.putObject(LASER.getLoc(), LASER);
    }
}
