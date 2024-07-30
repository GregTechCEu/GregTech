package gregtech.client.renderer.pipe;

import gregtech.api.block.UnlistedByteProperty;
import gregtech.api.block.UnlistedFloatProperty;
import gregtech.api.block.UnlistedIntegerProperty;
import gregtech.api.block.UnlistedPropertyMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cache.ColorQuadCache;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.CacheKey;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public abstract class AbstractPipeModel<K extends CacheKey> implements IBakedModel {

    public static UnlistedFloatProperty THICKNESS_PROPERTY = new UnlistedFloatProperty("thickness");

    public static UnlistedPropertyMaterial FRAME_MATERIAL_PROPERTY = new UnlistedPropertyMaterial("frame_material");
    public static UnlistedByteProperty FRAME_MASK_PROPERTY = new UnlistedByteProperty("frame_mask");

    public static UnlistedByteProperty CONNECTION_MASK_PROPERTY = new UnlistedByteProperty("connection_mask");
    public static UnlistedByteProperty CLOSED_MASK_PROPERTY = new UnlistedByteProperty("closed_mask");
    public static UnlistedByteProperty BLOCKED_MASK_PROPERTY = new UnlistedByteProperty("blocked_mask");
    public static UnlistedIntegerProperty COLOR_PROPERTY = new UnlistedIntegerProperty("color");

    protected final Object2ObjectOpenHashMap<ResourceLocation, ColorQuadCache> frameCache = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<K, StructureQuadCache> pipeCache = new Object2ObjectOpenHashMap<>();

    private final ModelResourceLocation loc;

    public AbstractPipeModel(ModelResourceLocation loc) {
        this.loc = loc;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side == null && state instanceof IExtendedBlockState ext) {
            return getQuads(toKey(ext), safeByte(ext.getValue(CONNECTION_MASK_PROPERTY)),
                    safeByte(ext.getValue(CLOSED_MASK_PROPERTY)), safeByte(ext.getValue(BLOCKED_MASK_PROPERTY)),
                    safeInt(ext.getValue(COLOR_PROPERTY)), ext.getValue(FRAME_MATERIAL_PROPERTY),
                    safeByte(ext.getValue(FRAME_MASK_PROPERTY)));
        }
        return Collections.emptyList();
    }

    protected static byte safeByte(@Nullable Byte abyte) {
        return abyte == null ? 0 : abyte;
    }

    protected static int safeInt(@Nullable Integer integer) {
        return integer == null ? 0 : integer;
    }

    public @NotNull List<BakedQuad> getQuads(K key, byte connectionMask, byte closedMask, byte blockedMask, int argb,
                                             @Nullable Material frameMaterial, byte frameMask) {
        List<BakedQuad> quads = new ObjectArrayList<>();

        StructureQuadCache cache = pipeCache.get(key);
        if (cache == null) {
            cache = constructForKey(key);
            pipeCache.put(key, cache);
        }
        cache.addToList(quads, connectionMask, closedMask,
                blockedMask, argb);

        if (frameMaterial != null) {
            ResourceLocation rl = MaterialIconType.frameGt.getBlockTexturePath(frameMaterial.getMaterialIconSet());
            ColorQuadCache frame = frameCache.get(rl);
            if (frame == null) {
                frame = new ColorQuadCache(PipeQuadHelper
                        .createFrame(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString())));
                frameCache.put(rl, frame);
            }
            List<BakedQuad> frameQuads = frame.getQuads(GTUtility.convertRGBtoARGB(frameMaterial.getMaterialRGB()));
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

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    public ModelResourceLocation getLoc() {
        return loc;
    }

    @Nullable
    protected abstract PipeItemModel<K> getItemModel(@NotNull ItemStack stack, World world, EntityLivingBase entity);

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return FakeItemOverrideList.INSTANCE;
    }

    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    protected static BlockRenderLayer getCurrentRenderLayer() {
        return MinecraftForgeClient.getRenderLayer();
    }

    protected static class FakeItemOverrideList extends ItemOverrideList {

        public static final FakeItemOverrideList INSTANCE = new FakeItemOverrideList();

        @Override
        public @NotNull IBakedModel handleItemState(@NotNull IBakedModel originalModel, @NotNull ItemStack stack,
                                                    World world,
                                                    EntityLivingBase entity) {
            if (originalModel instanceof AbstractPipeModel<?>model) {
                PipeItemModel<?> item = model.getItemModel(stack, world, entity);
                if (item != null) return item;
            }
            return originalModel;
        }
    }
}
