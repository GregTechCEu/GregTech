package gregtech.api.model.customtexture;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Copyright CTM.
 */
public enum CustomTextureModelHandler implements IResourceManagerReloadListener {
    INSTANCE;

    private final Set<ResourceLocation> wrappedModels = Sets.newHashSet();

    @SubscribeEvent(priority = EventPriority.LOWEST) // low priority to capture all event-registered models
    public void onModelBake(ModelBakeEvent event) {
        Map<ModelResourceLocation, IModel> stateModels = ObfuscationReflectionHelper.getPrivateValue(ModelLoader.class, event.getModelLoader(), "stateModels");
        for (ModelResourceLocation mrl : event.getModelRegistry().getKeys()) {
            if (!wrappedModels.contains(mrl)) {
                IModel rootModel = stateModels.get(mrl);
                if (rootModel != null && !(rootModel instanceof CustomTextureModel)) {
                    Set<ResourceLocation> textures = Sets.newHashSet(rootModel.getTextures());
                    for (ResourceLocation tex : textures) {
                        MetadataSectionCTM meta = null;
                        try {
                            meta = getMetadata(spriteToAbsolute(tex));
                        } catch (IOException ignored) {} // Fallthrough
                        if (meta != null) {
                            wrappedModels.add(mrl);
                            event.getModelRegistry().putObject(mrl, wrap(rootModel, event.getModelRegistry().getObject(mrl)));
                            break;
                        }
                    }
                }
            }
        }
    }

    private @Nonnull
    IBakedModel wrap(IModel model, IBakedModel object) {
        CustomTextureModel ctm = new CustomTextureModel(null, model);
        ctm.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, rl -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString()));
        return new CustomTextureBakedModel(ctm, object);
    }

    private static final Map<ResourceLocation, MetadataSectionCTM> metadataCache = new HashMap<>();

    public static ResourceLocation spriteToAbsolute(ResourceLocation sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = new ResourceLocation(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    @Nullable
    public static MetadataSectionCTM getMetadata(ResourceLocation res) throws IOException {
        if (metadataCache.containsKey(res)) {
            return metadataCache.get(res);
        }
        MetadataSectionCTM ret;
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(res)) {
            ret = resource.getMetadata(MetadataSectionCTM.SECTION_NAME);
        } catch (FileNotFoundException e) {
            ret = null;
        } catch (JsonParseException e) {
            throw new IOException("Error loading metadata for location " + res, e);
        }
        metadataCache.put(res, ret);
        return ret;
    }

    @Nullable
    public static MetadataSectionCTM getMetadata(TextureAtlasSprite sprite) throws IOException {
        return getMetadata(spriteToAbsolute(new ResourceLocation(sprite.getIconName())));
    }

    @Nullable
    public static MetadataSectionCTM getMetadataUnsafe(TextureAtlasSprite sprite) {
        try {
            return getMetadata(sprite);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        metadataCache.clear();
        CustomTextureBakedModel.invalidateCaches();;
        wrappedModels.clear();
    }
}
