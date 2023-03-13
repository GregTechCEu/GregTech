package gregtech.client.model;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class OreBakedModel implements IBakedModel {

    private static final Map<Entry, ModelResourceLocation> ENTRIES = new Object2ObjectOpenHashMap<>();

    public static ModelResourceLocation registerOreEntry(StoneType stoneType, Material material) {
        return ENTRIES.computeIfAbsent(
                new Entry(stoneType, material.getMaterialIconSet(), material.getProperty(PropertyKey.ORE).isEmissive()),
                Entry::getModelId);
    }

    protected final StoneType stoneType;
    protected final IBakedModel overlay;

    private IBakedModel baseModel;

    public OreBakedModel(StoneType stoneType, IBakedModel overlay) {
        this.stoneType = stoneType;
        this.overlay = overlay;
    }

    protected IBakedModel getBaseModel() {
        if (this.baseModel == null) {
            return this.baseModel = Minecraft.getMinecraft().blockRenderDispatcher
                    .getModelForState(this.stoneType.stone.get());
        }
        return this.baseModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        // a way to guarantee one variant on random models with arbitrary entries.
        // this essentially prevents z-fighting issues as long as the first model defined in weighted baked model
        // does not have any rotation applied.
        List<BakedQuad> quads = new ArrayList<>(getBaseModel().getQuads(null, side, 0));
        quads.addAll(this.overlay.getQuads(null, side, rand));
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return getBaseModel().isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return getBaseModel().isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getBaseModel().getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return getBaseModel().getItemCameraTransforms();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return getBaseModel().isAmbientOcclusion(state);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Map.Entry<Entry, ModelResourceLocation> e : ENTRIES.entrySet()) {
            event.getMap().registerSprite(MaterialIconType.ore.getBlockTexturePath(e.getKey().iconSet));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> overlayCache = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<Entry, ModelResourceLocation> e : ENTRIES.entrySet()) {
            IBakedModel overlay = overlayCache.computeIfAbsent(MaterialIconType.ore.getBlockTexturePath(e.getKey().iconSet),
                    tex -> new ModelFactory(ModelFactory.ModelTemplate.ORE_OVERLAY)
                            .addSprite("texture", tex)
                            .bake());
            event.getModelRegistry().putObject(e.getValue(), e.getKey().emissive ?
                    new EmissiveOreBakedModel(e.getKey().stoneType, overlay) :
                    new OreBakedModel(e.getKey().stoneType, overlay));
        }
    }

    public static class EmissiveOreBakedModel extends OreBakedModel {

        @SuppressWarnings("unchecked")
        private final List<BakedQuad>[] overlayQuads = new List[7];

        public EmissiveOreBakedModel(StoneType stoneType, IBakedModel overlay) {
            super(stoneType, overlay);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

            if (state == null || layer == null || !ConfigHolder.client.shader.useShader) {
                List<BakedQuad> quads = new ArrayList<>(getBaseModel().getQuads(null, side, 0));
                quads.addAll(getOverlayQuads(side, rand));
                return quads;
            } else if (layer == BlockRenderLayer.CUTOUT_MIPPED) {
                return getBaseModel().getQuads(null, side, 0);
            } else if (layer == BloomEffectUtil.getRealBloomLayer()) {
                return getOverlayQuads(side, rand);
            } else {
                return Collections.emptyList();
            }
        }

        protected List<BakedQuad> getOverlayQuads(@Nullable EnumFacing side, long rand) {
            int index = side == null ? 6 : side.getIndex();
            if (this.overlayQuads[index] == null) {
                List<BakedQuad> quads = new ArrayList<>(this.overlay.getQuads(null, side, rand));
                for (int i = 0; i < quads.size(); i++) {
                    quads.set(i, transform(quads.get(i)));
                }
                return this.overlayQuads[index] = quads;
            }
            return this.overlayQuads[index];
        }

        private static BakedQuad transform(BakedQuad quad) {
            if (FMLClientHandler.instance().hasOptifine()) return quad;
            VertexFormat format = quad.getFormat();
            if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) {
                format = new VertexFormat(quad.getFormat());
                format.addElement(DefaultVertexFormats.TEX_2S);
            }
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format) {
                @Override
                public void put(int element, float... data) {
                    if (this.getVertexFormat().getElement(element) == DefaultVertexFormats.TEX_2S)
                        super.put(element, 480.0f / 0xFFFF, 480.0f / 0xFFFF);
                    else super.put(element, data);
                }
            };
            quad.pipe(builder);
            builder.setApplyDiffuseLighting(false);
            return builder.build();
        }
    }

    private static final class Entry {

        private final StoneType stoneType;
        private final MaterialIconSet iconSet;
        private final boolean emissive;

        private Entry(StoneType stoneType, MaterialIconSet iconSet, boolean emissive) {
            this.stoneType = stoneType;
            this.iconSet = iconSet;
            this.emissive = emissive;
        }

        public ModelResourceLocation getModelId() {
            return new ModelResourceLocation(new ResourceLocation(GTValues.MODID,
                    "ore_" + this.stoneType.name + "_" + this.iconSet.name + (this.emissive ? "_emissive" : "")), "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return this.stoneType.name.equals(entry.stoneType.name) &&
                    this.iconSet.name.equals(entry.iconSet.name) &&
                    this.emissive == entry.emissive;
        }

        @Override
        public int hashCode() {
            return Objects.hash(stoneType.name, iconSet.name, emissive);
        }

        @Override
        public String toString() {
            return "stoneType=" + stoneType.name + ", iconSet=" + iconSet.name + ", emissive=" + emissive;
        }
    }
}
