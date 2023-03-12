package gregtech.client.model.modelfactories;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class MaterialBlockBakedModel implements IBakedModel {

    private static final Table<MaterialIconType, MaterialIconSet, MaterialBlockBakedModel> INSTANCES = HashBasedTable.create();

    private static final Map<ModelResourceLocation, IModel> UNBAKED_MODEL_CACHE = new Object2ObjectOpenHashMap<>();

    public static MaterialBlockBakedModel get(MaterialIconType iconType, Material material) {
        return get(iconType, material.getMaterialIconSet());
    }

    public static MaterialBlockBakedModel get(MaterialIconType iconType, MaterialIconSet iconSet) {
        MaterialBlockBakedModel model = INSTANCES.get(iconType, iconSet);
        if (model == null) {
            model = new MaterialBlockBakedModel(iconType, iconSet);
            INSTANCES.put(iconType, iconSet, model);
        }
        return model;
    }

    private final MaterialIconSet iconSet;
    private final MaterialIconType iconType;

    private final ModelResourceLocation bakedModelId;

    protected MaterialBlockBakedModel(MaterialIconType iconType, MaterialIconSet iconSet) {
        this.iconSet = iconSet;
        this.iconType = iconType;

        this.bakedModelId = new ModelResourceLocation(new ResourceLocation(GTValues.MODID,
                "material_block_" + iconSet.name + "_" + iconType.name), "");
    }

    public ModelResourceLocation getBakedModelId() {
        return bakedModelId;
    }

    public ResourceLocation getBaseModelLocation() {
        return this.iconType.getBlockModelPath(this.iconSet);
    }

    public ModelResourceLocation getModelLocation(String variant) {
        return new ModelResourceLocation(getBaseModelLocation(), variant);
    }

    protected IBakedModel getModel() {
        return Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager()
                .getModel(getModelLocation(""));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return getModel().getQuads(state, side, rand);
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
        return getModel().isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getModel().getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return getModel().getOverrides();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return getModel().getItemCameraTransforms();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return getModel().isAmbientOcclusion(state);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return getModel().handlePerspective(cameraTransformType);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        UNBAKED_MODEL_CACHE.clear(); // in case of state desync
        for (MaterialBlockBakedModel model : INSTANCES.values()) {
            IModel baseModel;
            boolean error = false;
            try {
                baseModel = ModelLoaderRegistry.getModel(model.getBaseModelLocation());
            } catch (Exception e) {
                GTLog.logger.error("Failed to load material model {}:", model.getBaseModelLocation(), e);
                baseModel = ModelLoaderRegistry.getMissingModel();
                error = true;
            }

            if (!error) {
                for (ResourceLocation texture : baseModel.getTextures()) {
                    event.getMap().registerSprite(texture);
                }
            }
            UNBAKED_MODEL_CACHE.put(model.getModelLocation(""), baseModel);
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (MaterialBlockBakedModel model : INSTANCES.values()) {
            event.getModelRegistry().putObject(model.getBakedModelId(), model);
        }
        for (Map.Entry<ModelResourceLocation, IModel> e : UNBAKED_MODEL_CACHE.entrySet()) {
            IBakedModel baked = e.getValue().bake(
                    e.getValue().getDefaultState(),
                    DefaultVertexFormats.ITEM,
                    t -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(t.toString()));
            event.getModelRegistry().putObject(e.getKey(), baked);
        }
        UNBAKED_MODEL_CACHE.clear();
    }
}
