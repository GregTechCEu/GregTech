package gregtech.client.renderer.pipe;

import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.pipe.cache.ActivableSQC;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.ActivableCacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class ActivablePipeModel extends AbstractPipeModel<ActivableCacheKey> {

    private final Supplier<SpriteInformation> inTex;
    private final Supplier<SpriteInformation> sideTex;
    private final Supplier<SpriteInformation> overlayTex;
    private final Supplier<SpriteInformation> overlayActiveTex;

    private final boolean emissiveActive;

    public ActivablePipeModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex,
                              @NotNull Supplier<SpriteInformation> overlayTex,
                              @NotNull Supplier<SpriteInformation> overlayActiveTex, boolean emissiveActive) {
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.overlayTex = overlayTex;
        this.overlayActiveTex = overlayActiveTex;
        this.emissiveActive = emissiveActive;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(ActivableCacheKey key, byte connectionMask, byte closedMask,
                                             byte blockedMask, ColorData data, @Nullable Material frameMaterial,
                                             byte frameMask, byte coverMask) {
        boolean bloomLayer = getCurrentRenderLayer() == BloomEffectUtil.getEffectiveBloomLayer();
        // don't render the main shape to the bloom layer
        List<BakedQuad> quads = bloomLayer ? new ObjectArrayList<>() :
                super.getQuads(key, connectionMask, closedMask, blockedMask, data, frameMaterial, frameMask, coverMask);

        if (key.isActive() && allowActive()) {
            if (emissiveActive && bloomLayer) {
                ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, data, true);
                // TODO bake this into the original quads
                quads = quads.stream().map(RenderUtil::makeEmissive).collect(Collectors.toList());
            } else if (!emissiveActive && !bloomLayer) {
                ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, data, true);
            }
        } else if (!bloomLayer) {
            ((ActivableSQC) pipeCache.get(key)).addOverlay(quads, connectionMask, data, false);
        }
        return quads;
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideTex.get();
    }

    @Override
    protected @NotNull ActivableCacheKey toKey(@NotNull IExtendedBlockState state) {
        return ActivableCacheKey.of(state.getValue(PipeRenderProperties.THICKNESS_PROPERTY), state.getValue(
                PipeRenderProperties.ACTIVE_PROPERTY));
    }

    @Override
    protected StructureQuadCache constructForKey(ActivableCacheKey key) {
        return ActivableSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get(),
                overlayTex.get(), overlayActiveTex.get());
    }

    @Override
    protected boolean shouldRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED ||
                (allowActive() && emissiveActive && layer == BloomEffectUtil.getEffectiveBloomLayer());
    }

    public boolean allowActive() {
        return !ConfigHolder.client.preventAnimatedCables;
    }

    @Override
    protected @Nullable PipeItemModel<ActivableCacheKey> getItemModel(PipeModelRedirector redirector,
                                                                      @NotNull ItemStack stack, World world,
                                                                      EntityLivingBase entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        return new PipeItemModel<>(redirector, this,
                new ActivableCacheKey(block.getStructure().getRenderThickness(), false),
                new ColorData(PipeTileEntity.DEFAULT_COLOR));
    }
}
