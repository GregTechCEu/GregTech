package gregtech.client.model;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Revamped from https://github.com/LoliKingdom/Zairyou/blob/main/src/main/java/zone/rong/zairyou/api/client/Bakery.java
 */
@SideOnly(Side.CLIENT)
public class ModelFactory {

    private static final Map<ItemCameraTransforms.TransformType, TRSRTransformation> blockTransformationMap = new EnumMap<>(ItemCameraTransforms.TransformType.class);
    private static final Map<ItemCameraTransforms.TransformType, TRSRTransformation> itemTransformationMap = new EnumMap<>(ItemCameraTransforms.TransformType.class);

    private static FaceBakery INSTANCE;

    static {
        blockTransformationMap.put(ItemCameraTransforms.TransformType.GUI, getTransform(0, 0, 0, 30, 225, 0, 0.625f));
        blockTransformationMap.put(ItemCameraTransforms.TransformType.GROUND, getTransform(0, 2, 0, 0, 0, 0, 0.25f));
        blockTransformationMap.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(0, 0, 0, 0, 45, 0, 0.4f));
        blockTransformationMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, getTransform(0, 0, 0, 0, 0, 0, 0.4f));
        blockTransformationMap.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, getTransform(0, 0, 0, 45, 0, 0, 0.4f));
        blockTransformationMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, getTransform(0, 0, 0, 45, 0, 0, 0.4f));

        itemTransformationMap.put(ItemCameraTransforms.TransformType.GUI, getTransform(0, 0, 0, 0, 0, 0, 1f));
        itemTransformationMap.put(ItemCameraTransforms.TransformType.GROUND, getTransform(0, 2, 0, 0, 0, 0, 0.5f));
        itemTransformationMap.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
        itemTransformationMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, getTransform(0, 3, 1, 0, 0, 0, 0.55f));
        itemTransformationMap.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, getTransform(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));
        itemTransformationMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, getTransform(0f, 4.0f, 0.5f, 0, 90, -55, 0.85f));
    }

    public static FaceBakery getBakery() {
        if (INSTANCE == null) {
            INSTANCE = new FaceBakery();
        }
        return INSTANCE;
    }

    public static TRSRTransformation getBlockTransform(ItemCameraTransforms.TransformType transformType) {
        return blockTransformationMap.get(transformType);
    }

    public static TRSRTransformation getItemTransform(ItemCameraTransforms.TransformType transformType) {
        return itemTransformationMap.get(transformType);
    }

    private static TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(new Vector3f(tx / 16, ty / 16, tz / 16), TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)), new Vector3f(s, s, s), null);
    }

    private final ModelTemplate template;
    private final Map<String, String> sprites;

    private IModelState state;
    private VertexFormat format;
    private UnaryOperator<IModel> mutation;

    public ModelFactory(ModelTemplate template) {
        this.template = template;
        this.sprites = new Object2ObjectOpenHashMap<>();
        this.state = template.model.getDefaultState();
        this.format = DefaultVertexFormats.BLOCK;
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

    public ModelFactory addParticleSprite(ResourceLocation textureLocation) {
        return addParticleSprite(textureLocation.toString());
    }

    public ModelFactory addParticleSprite(String textureLocation) {
        this.sprites.put("particle", textureLocation);
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
        IModel mapped = template.model.retexture(ImmutableMap.copyOf(sprites));
        if (mutation != null) {
            mutation.apply(mapped);
        }
        return mapped.bake(mapped.getDefaultState(), format, ModelLoader.defaultTextureGetter());
    }

    public static class ModelTemplate {

        public static final ModelTemplate CUBE_2_LAYER_ALL_TINT_INDEX = new ModelTemplate(GTValues.MODID, "block/cube_2_layer_all_tintindex");
        public static final ModelTemplate BLOCK = new ModelTemplate("minecraft", "block/block");
        public static final ModelTemplate NORMAL_ITEM = new ModelTemplate("minecraft", "item/generated");
        public static final ModelTemplate HANDHELD_ITEM = new ModelTemplate("minecraft", "item/handheld");

        private final IModel model;

        public ModelTemplate(String locationDomain, String locationPath) {
            this.model = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation(locationDomain, locationPath));
        }

        public IModel getModel() {
            return model;
        }
    }
}
