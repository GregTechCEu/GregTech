package gregtech.client.model.modelfactories;

import gregtech.api.GTValues;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class MaterialBlockModelLoader {

    private static final Table<MaterialIconType, MaterialIconSet, Entry> ENTRIES = HashBasedTable.create();

    private static final Map<ModelResourceLocation, IModel> UNBAKED_MODEL_CACHE = new Object2ObjectOpenHashMap<>();

    public static ModelResourceLocation registerBlockModel(MaterialIconType iconType, MaterialIconSet iconSet) {
        return register(iconType, iconSet).blockModelId;
    }

    public static ModelResourceLocation registerItemModel(MaterialIconType iconType, MaterialIconSet iconSet) {
        return register(iconType, iconSet).itemModelId;
    }

    private static Entry register(MaterialIconType iconType, MaterialIconSet iconSet) {
        Entry e = ENTRIES.get(iconType, iconSet);
        if (e == null) {
            e = new Entry(iconType, iconSet);
            ENTRIES.put(iconType, iconSet, e);
        }
        return e;
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        UNBAKED_MODEL_CACHE.clear(); // in case of state desync
        for (Entry e : ENTRIES.values()) {
            loadModel(event, e.getBlockModelLocation(), e.blockModelId);
            loadModel(event, e.getItemModelLocation(), e.itemModelId);
        }
    }

    private static void loadModel(TextureStitchEvent.Pre event, ResourceLocation modelLocation,
                                  ModelResourceLocation modelId) {
        IModel model;
        try {
            model = ModelLoaderRegistry.getModel(modelLocation);
        } catch (Exception e) {
            GTLog.logger.error("Failed to load material model {}:", modelLocation, e);
            UNBAKED_MODEL_CACHE.put(modelId, ModelLoaderRegistry.getMissingModel());
            return;
        }
        for (ResourceLocation texture : model.getTextures()) {
            event.getMap().registerSprite(texture);
        }
        UNBAKED_MODEL_CACHE.put(modelId, model);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (Map.Entry<ModelResourceLocation, IModel> e : UNBAKED_MODEL_CACHE.entrySet()) {
            IBakedModel baked = e.getValue().bake(
                    e.getValue().getDefaultState(),
                    DefaultVertexFormats.ITEM,
                    t -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(t.toString()));
            event.getModelRegistry().putObject(e.getKey(), baked);
        }
        UNBAKED_MODEL_CACHE.clear();
    }

    private static final class Entry {

        final MaterialIconType iconType;
        final MaterialIconSet iconSet;

        final ModelResourceLocation blockModelId;
        final ModelResourceLocation itemModelId;

        Entry(MaterialIconType iconType, MaterialIconSet iconSet) {
            this.iconType = iconType;
            this.iconSet = iconSet;

            this.blockModelId = new ModelResourceLocation(
                    GTUtility.gregtechId("material_" + iconType.name + "_" + iconSet.name), "normal");
            this.itemModelId = new ModelResourceLocation(
                    GTUtility.gregtechId("material_" + iconType.name + "_" + iconSet.name), "inventory");
        }

        ResourceLocation getBlockModelLocation() {
            return iconType.getBlockModelPath(iconSet);
        }

        ResourceLocation getItemModelLocation() {
            ResourceLocation itemModelPath = iconType.getItemModelPath(iconSet);
            return new ResourceLocation(itemModelPath.getNamespace(), "item/" + itemModelPath.getPath());
        }
    }
}
