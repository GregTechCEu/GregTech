package gregtech.client.model.modelfactories;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.client.utils.BloomEffectUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class ActiveVariantBlockBakedModel implements IBakedModel {

    private static final Map<ModelResourceLocation, ActiveVariantBlockBakedModel> INSTANCES = new Object2ObjectOpenHashMap<>();
    private static final String[] BLOOM_TEXTURE_SUFFIX = {"_bloom", "_emissive", "_bloom_ctm", "_emissive_ctm"};

    private final ModelResourceLocation inactiveModelLocation;
    private final ModelResourceLocation activeModelLocation;
    @Nullable
    private final BooleanSupplier bloomConfig;

    private final ModelResourceLocation modelLocation;

    public ActiveVariantBlockBakedModel(ModelResourceLocation inactiveModelLocation, ModelResourceLocation activeModelLocation, @Nullable BooleanSupplier bloomConfig) {
        this.inactiveModelLocation = inactiveModelLocation;
        this.activeModelLocation = activeModelLocation;
        this.bloomConfig = bloomConfig;
        this.modelLocation = new ModelResourceLocation(
                new ResourceLocation(GTValues.MODID, "active_variant_block_" + inactiveModelLocation.getNamespace() + "_" + inactiveModelLocation.getPath()),
                inactiveModelLocation.getVariant().replaceAll(",active=(?:true|false)|active=(?:true|false),?", ""));
        INSTANCES.put(modelLocation, this);
    }

    public ModelResourceLocation getModelLocation() {
        return modelLocation;
    }

    protected boolean getBloomConfig() {
        return bloomConfig == null || bloomConfig.getAsBoolean();
    }

    protected IBakedModel getModel(IBlockState state) {
        //Some mods like to call this without getting the extendedBlockState leading to a NPE crash since the
        //unlisted ACTIVE property is null.
        return getModel(Boolean.TRUE.equals(((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE)));
    }

    protected IBakedModel getModel(boolean active) {
        return Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager()
                .getModel(active ? activeModelLocation : inactiveModelLocation);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null) return Collections.emptyList();

        IBakedModel m = getModel(state);

        // If bloom is enabled, bloom textures are rendered on bloom layer
        // If bloom is disabled (either by model specific bloom config or the presence of O**ifine shaders)
        // it is rendered on CUTOUT layer instead.
        if (getBloomConfig()) {
            return MinecraftForgeClient.getRenderLayer() == BloomEffectUtil.BLOOM ?
                    getBloomQuads(m, state, side, rand) :
                    m.getQuads(state, side, rand);
        } else {
            if (MinecraftForgeClient.getRenderLayer() == BloomEffectUtil.BLOOM) {
                return Collections.emptyList();
            } else if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT) {
                List<BakedQuad> quads = new ArrayList<>(m.getQuads(state, side, rand));
                ForgeHooksClient.setRenderLayer(BloomEffectUtil.BLOOM);
                quads.addAll(getBloomQuads(m, state, side, rand));
                ForgeHooksClient.setRenderLayer(BlockRenderLayer.CUTOUT);
                return quads;
            } else return m.getQuads(state, side, rand);
        }
    }

    private static List<BakedQuad> getBloomQuads(IBakedModel model, @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> list = new ArrayList<>();
        for (BakedQuad q : model.getQuads(state, side, rand)) {
            for (String bloomTextureSuffix : BLOOM_TEXTURE_SUFFIX) {
                if (q.getSprite().getIconName().endsWith(bloomTextureSuffix)) {
                    list.add(transform(q));
                    break;
                }
            }
        }
        return list;
    }

    private static BakedQuad transform(BakedQuad quad) {
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
        return builder.build();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return getModel(false).isAmbientOcclusion();
    }

    @Override
    public boolean isAmbientOcclusion(@Nonnull IBlockState state) {
        return getModel(state).isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return getModel(false).isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getModel(false).getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        INSTANCES.forEach(event.getModelRegistry()::putObject);
    }
}
