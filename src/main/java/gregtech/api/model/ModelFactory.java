package gregtech.api.model;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Revamped from https://github.com/LoliKingdom/Zairyou/blob/main/src/main/java/zone/rong/zairyou/api/client/Bakery.java
 */
public class ModelFactory {

    private final ModelTemplate template;
    private final String particleLocation;
    private final Map<String, String> sprites;

    private VertexFormat format = DefaultVertexFormats.BLOCK;
    private UnaryOperator<IModel> mutation;

    public ModelFactory(ModelTemplate template, ResourceLocation particleLocation) {
        this(template, particleLocation.toString());
    }

    public ModelFactory(ModelTemplate template, String particleLocation) {
        this.template = template;
        this.particleLocation = particleLocation;
        this.sprites = new Object2ObjectOpenHashMap<>();
    }

    public ModelFactory addSpriteToLayer(int layer, ResourceLocation textureLocation) {
        return addSpriteToLayer(layer, textureLocation.toString());
    }

    public ModelFactory addSpriteToLayer(int layer, String textureLocation) {
        this.sprites.put("layer" + layer, textureLocation);
        return this;
    }

    public ModelFactory addSprite(String element, ResourceLocation textureLocation) {
        return addSprite(element, textureLocation.toString());
    }

    public ModelFactory addSprite(String element, String textureLocation) {
        this.sprites.put(element, textureLocation);
        return this;
    }

    public ModelFactory changeFormat(VertexFormat format) {
        this.format = format;
        return this;
    }

    public ModelFactory mutateModel(UnaryOperator<IModel> mutate) {
        this.mutation = mutate;
        return this;
    }

    public IBakedModel bake() {
        if (!sprites.containsKey("particle")) {
            sprites.put("particle", particleLocation);
        }
        IModel mapped = template.model.retexture(ImmutableMap.copyOf(sprites));
        if (mutation != null) {
            mutation.apply(mapped);
        }
        return mapped.bake(mapped.getDefaultState(), format, ModelLoader.defaultTextureGetter());
    }

    public static class ModelTemplate {

        public static IModel load(String domain, String path) {
            return load(new ResourceLocation(domain, path));
        }

        public static IModel load(ResourceLocation loc) {
            return ModelLoaderRegistry.getModelOrMissing(loc);
        }

        public static final ModelTemplate DOUBLE_LAYERED_BLOCK = new ModelTemplate(GTValues.MODID, "block/double_layered_block");
        public static final ModelTemplate NORMAL_ITEM = new ModelTemplate("minecraft", "item/generated");
        public static final ModelTemplate HANDHELD_ITEM = new ModelTemplate("minecraft", "item/handheld");

        private final IModel model;

        public ModelTemplate(String locationDomain, String locationPath) {
            this.model = load(locationDomain, locationPath);
        }

        public ModelTemplate(ResourceLocation location) {
            this.model = load(location);
        }

        public ModelTemplate(IModel model) {
            this.model = model;
        }

        public IModel getModel() {
            return model;
        }

    }

}
