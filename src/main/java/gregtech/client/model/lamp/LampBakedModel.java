package gregtech.client.model.lamp;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// TODO could probably be combined with new OreBakedModel or AVBBM
@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class LampBakedModel implements IBakedModel {

    private static final String[] BLOOM_TEXTURE_SUFFIX = { "_bloom", "_emissive", "_bloom_ctm", "_emissive_ctm" };
    private static final Map<Key, Entry> ENTRIES = new Object2ObjectOpenHashMap<>();

    public static Entry register(EnumDyeColor color, LampModelType modelType, boolean bloom, boolean active) {
        return ENTRIES.computeIfAbsent(new Key(color, modelType, bloom, active), Entry::new);
    }

    private final ModelResourceLocation modelLocation;

    private IBakedModel model;

    public LampBakedModel(ModelResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
    }

    public LampBakedModel(IBakedModel model) {
        this.modelLocation = null;
        this.model = model;
    }

    protected IBakedModel getModel() {
        if (this.model == null) {
            return this.model = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager()
                    .getModel(Objects.requireNonNull(modelLocation));
        }
        return this.model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (state == null || layer == null) {
            return getFilteredQuads(true, true, state, side, rand);
        } else if (layer == BlockRenderLayer.SOLID) {
            return getFilteredQuads(false, true, state, side, rand);
        } else if (layer == BloomEffectUtil.getBloomLayer() || layer == BlockRenderLayer.CUTOUT) {
            return getFilteredQuads(true, false, state, side, rand);
        } else {
            return Collections.emptyList();
        }
    }

    private List<BakedQuad> getFilteredQuads(boolean emissive, boolean nonEmissive, @Nullable IBlockState state,
                                             @Nullable EnumFacing side, long rand) {
        if (!emissive && !nonEmissive) return Collections.emptyList();
        List<BakedQuad> quads = new ArrayList<>();
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        ForgeHooksClient.setRenderLayer(null); // ctm
        List<BakedQuad> originalQuads = getModel().getQuads(state, side, rand);
        ForgeHooksClient.setRenderLayer(layer);
        for (BakedQuad q : originalQuads) {
            boolean isBloomTexture = false;
            for (String bloomTextureSuffix : BLOOM_TEXTURE_SUFFIX) {
                if (q.getSprite().getIconName().endsWith(bloomTextureSuffix)) {
                    isBloomTexture = true;
                    break;
                }
            }
            if (isBloomTexture) {
                if (emissive) quads.add(RenderUtil.makeEmissive(q));
            } else {
                if (nonEmissive) quads.add(q);
            }
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return getModel().isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return getModel().isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getModel().getParticleTexture();
    }

    @NotNull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return getModel().getItemCameraTransforms();
    }

    @Override
    public boolean isAmbientOcclusion(@NotNull IBlockState state) {
        return getModel().isAmbientOcclusion(state);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (Map.Entry<Key, Entry> e : ENTRIES.entrySet()) {
            Entry entry = e.getValue();
            if (entry.customItemModel != null) {
                IBakedModel model = event.getModelRegistry().getObject(entry.originalModelLocation);
                if (model != null) {
                    // Directly provide existing model to prevent using CTM models
                    IBakedModel customModel = e.getKey().modelType.createModel(model);
                    event.getModelRegistry().putObject(entry.customItemModel, customModel);
                }
            }
            if (entry.customBlockModel != null) {
                // Lazy populate model with model location to use CTM models
                IBakedModel customModel = e.getKey().modelType.createModel(entry.originalModelLocation);
                event.getModelRegistry().putObject(entry.customBlockModel, customModel);
            }
        }
    }

    private static final class Key {

        private final EnumDyeColor color;
        private final LampModelType modelType;
        private final boolean bloom;
        private final boolean active;

        private final int hash;

        private Key(EnumDyeColor color, LampModelType modelType, boolean bloom, boolean active) {
            this.color = color;
            this.modelType = modelType;
            this.bloom = bloom;
            this.active = active;

            this.hash = Objects.hash(color, modelType, bloom, active);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key entry = (Key) o;
            return bloom == entry.bloom &&
                    active == entry.active &&
                    color == entry.color &&
                    modelType.equals(entry.modelType);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "color=" + color.getName() + ", modelType=" + modelType + ", bloom=" + bloom + ", active=" + active;
        }
    }

    public static final class Entry {

        @Nullable
        private final ModelResourceLocation customItemModel;
        @Nullable
        private final ModelResourceLocation customBlockModel;

        private final ModelResourceLocation originalModelLocation;

        private Entry(Key key) {
            this.originalModelLocation = new ModelResourceLocation(key.modelType.modelName,
                    "active=" + key.active + ",color=" + key.color.getName());
            if (key.active) {
                String baseModelId = "active_lamp_" + key.modelType.modelName.getNamespace() +
                        "_" + key.modelType.modelName.getPath() + "_" + key.color.getName();
                this.customItemModel = new ModelResourceLocation(
                        GTUtility.gregtechId(baseModelId + "_item"), "");
                this.customBlockModel = new ModelResourceLocation(
                        GTUtility.gregtechId(baseModelId + "_block" + (key.bloom ? "_bloom" : "")), "");
            } else { // just use original model, no custom code required
                this.customItemModel = this.customBlockModel = null;
            }
        }

        public ModelResourceLocation getItemModelId() {
            return this.customItemModel != null ? this.customItemModel : this.originalModelLocation;
        }

        public ModelResourceLocation getBlockModelId() {
            return this.customBlockModel != null ? this.customBlockModel : this.originalModelLocation;
        }

        public ModelResourceLocation getOriginalModelLocation() {
            return this.originalModelLocation;
        }
    }
}
