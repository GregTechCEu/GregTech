package gregtech.client.renderer.pipe;

import gregtech.api.block.UnlistedByteProperty;
import gregtech.api.block.UnlistedFloatProperty;
import gregtech.api.block.UnlistedIntegerProperty;
import gregtech.api.block.UnlistedPropertyMaterial;
import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTUtility;
import gregtech.api.util.reference.WeakHashSet;
import gregtech.client.renderer.pipe.cache.ColorQuadCache;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.cover.CoverRendererPackage;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.CacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class AbstractPipeModel<K extends CacheKey> {

    public static UnlistedFloatProperty THICKNESS_PROPERTY = new UnlistedFloatProperty("thickness");

    public static UnlistedPropertyMaterial FRAME_MATERIAL_PROPERTY = new UnlistedPropertyMaterial("frame_material");
    public static UnlistedByteProperty FRAME_MASK_PROPERTY = new UnlistedByteProperty("frame_mask");

    public static UnlistedByteProperty CLOSED_MASK_PROPERTY = new UnlistedByteProperty("closed_mask");
    public static UnlistedByteProperty BLOCKED_MASK_PROPERTY = new UnlistedByteProperty("blocked_mask");

    public static UnlistedIntegerProperty COLOR_PROPERTY = new UnlistedIntegerProperty("color");
    public static final UnlistedPropertyMaterial MATERIAL_PROPERTY = new UnlistedPropertyMaterial("material");

    protected final Object2ObjectOpenHashMap<ResourceLocation, ColorQuadCache> frameCache = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<K, StructureQuadCache> pipeCache;

    protected static final WeakHashSet<Object2ObjectOpenHashMap<? extends CacheKey, StructureQuadCache>> PIPE_CACHES = new WeakHashSet<>();

    public static void invalidateCaches() {
        for (var cache : PIPE_CACHES) {
            cache.clear();
            cache.trim(16);
        }
    }

    public AbstractPipeModel() {
        pipeCache = new Object2ObjectOpenHashMap<>();
        PIPE_CACHES.add(pipeCache);
    }

    public @NotNull List<BakedQuad> getQuads(IExtendedBlockState state, EnumFacing side, long rand) {
        if (side == null) {
            List<BakedQuad> quads;
            ColorData data = computeColorData(state);
            CoverRendererPackage rendererPackage = state.getValue(CoverRendererPackage.PROPERTY);
            byte coverMask = rendererPackage == null ? 0 : rendererPackage.getMask();
            if (shouldRenderInLayer(getCurrentRenderLayer())) {
                quads = getQuads(toKey(state), PipeBlock.readConnectionMask(state),
                        safeByte(state.getValue(CLOSED_MASK_PROPERTY)), safeByte(state.getValue(BLOCKED_MASK_PROPERTY)),
                        data, state.getValue(FRAME_MATERIAL_PROPERTY),
                        safeByte(state.getValue(FRAME_MASK_PROPERTY)), coverMask);
            } else quads = new ObjectArrayList<>();
            if (rendererPackage != null) renderCovers(quads, rendererPackage, state);
            return quads;
        }
        return Collections.emptyList();
    }

    protected void renderCovers(List<BakedQuad> quads, @NotNull CoverRendererPackage rendererPackage,
                                @NotNull IExtendedBlockState ext) {
        int color = safeInt(ext.getValue(COLOR_PROPERTY));
        if (ext.getUnlistedProperties().containsKey(AbstractPipeModel.MATERIAL_PROPERTY)) {
            Material material = ext.getValue(AbstractPipeModel.MATERIAL_PROPERTY);
            if (material != null) {
                int matColor = GTUtility.convertRGBtoARGB(material.getMaterialRGB());
                if (color == 0 || color == matColor) {
                    // unpainted
                    color = ConfigHolder.client.defaultPaintingColor;
                }
            }
        }
        rendererPackage.addQuads(quads, getCurrentRenderLayer(), new ColorData(color));
    }

    protected ColorData computeColorData(@NotNull IExtendedBlockState ext) {
        return new ColorData(safeInt(ext.getValue(COLOR_PROPERTY)));
    }

    protected static byte safeByte(@Nullable Byte abyte) {
        return abyte == null ? 0 : abyte;
    }

    protected static int safeInt(@Nullable Integer integer) {
        return integer == null ? 0 : integer;
    }

    public @NotNull List<BakedQuad> getQuads(K key, byte connectionMask, byte closedMask, byte blockedMask,
                                             ColorData data,
                                             @Nullable Material frameMaterial, byte frameMask, byte coverMask) {
        List<BakedQuad> quads = new ObjectArrayList<>();

        StructureQuadCache cache = pipeCache.computeIfAbsent(key, this::constructForKey);
        cache.addToList(quads, connectionMask, closedMask,
                blockedMask, data, coverMask);

        if (frameMaterial != null) {
            ResourceLocation rl = MaterialIconType.frameGt.getBlockTexturePath(frameMaterial.getMaterialIconSet());
            ColorQuadCache frame = frameCache.get(rl);
            if (frame == null) {
                frame = new ColorQuadCache(PipeQuadHelper
                        .createFrame(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString())));
                frameCache.put(rl, frame);
            }
            List<BakedQuad> frameQuads = frame
                    .getQuads(new ColorData(GTUtility.convertRGBtoARGB(frameMaterial.getMaterialRGB())));
            for (int i = 0; i < 6; i++) {
                if ((frameMask & (1 << i)) > 0) {
                    quads.add(frameQuads.get(i));
                }
            }
        }
        return quads;
    }

    protected abstract @NotNull K toKey(@NotNull IExtendedBlockState state);

    protected final @NotNull CacheKey defaultKey(@NotNull IExtendedBlockState state) {
        return CacheKey.of(state.getValue(THICKNESS_PROPERTY));
    }

    protected abstract StructureQuadCache constructForKey(K key);

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(int paintColor, @Nullable Material material) {
        SpriteInformation spriteInformation = getParticleSprite(material);
        return new ImmutablePair<>(spriteInformation.sprite(), spriteInformation.colorable() ? paintColor : 0xFFFFFFFF);
    }

    public abstract SpriteInformation getParticleSprite(@Nullable Material material);

    @Nullable
    protected abstract PipeItemModel<K> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                     World world, EntityLivingBase entity);

    protected boolean shouldRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    protected static BlockRenderLayer getCurrentRenderLayer() {
        return MinecraftForgeClient.getRenderLayer();
    }
}
