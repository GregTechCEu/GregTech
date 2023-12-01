package gregtech.client.model.miningpipe;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public final class SimpleMiningPipeModel implements MiningPipeModel {

    private static final Map<String, SimpleMiningPipeModel> MINING_PIPE_MODELS = new Object2ObjectOpenHashMap<>();
    private static final Supplier<IBakedModel> MISSING_MODEL_MEMOIZE = Suppliers.memoize(() -> {
        IModel model = ModelLoaderRegistry.getMissingModel();
        return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
    });

    @NotNull
    public static SimpleMiningPipeModel register(@NotNull String type) {
        return MINING_PIPE_MODELS.computeIfAbsent(type, SimpleMiningPipeModel::new);
    }

    @NotNull
    public final String type;

    @Nullable
    private IBakedModel baseModel;
    @Nullable
    private IModel unbakedBaseModel;
    @Nullable
    private IBakedModel bottomModel;
    @Nullable
    private IModel unbakedBottomModel;

    private SimpleMiningPipeModel(@NotNull String type) {
        this.type = type;
    }

    @NotNull
    @Override
    public IBakedModel getBaseModel() {
        return this.baseModel != null ? this.baseModel : MISSING_MODEL_MEMOIZE.get();
    }

    @NotNull
    @Override
    public IBakedModel getBottomModel() {
        return this.bottomModel != null ? this.bottomModel : MISSING_MODEL_MEMOIZE.get();
    }

    @Override
    public String toString() {
        return "MiningPipeModel{type='" + type + "'}";
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (SimpleMiningPipeModel miningPipe : MINING_PIPE_MODELS.values()) {
            miningPipe.unbakedBaseModel = loadModel(event,
                    GTUtility.gregtechId("block/mining_pipe/" + miningPipe.type));
            miningPipe.unbakedBottomModel = loadModel(event,
                    GTUtility.gregtechId("block/mining_pipe/" + miningPipe.type + "_bottom"));
        }
    }

    @Nullable
    private static IModel loadModel(TextureStitchEvent.Pre event, ResourceLocation modelLocation) {
        IModel model;
        try {
            model = ModelLoaderRegistry.getModel(modelLocation);
        } catch (Exception e) {
            GTLog.logger.error("Failed to load material model {}:", modelLocation, e);
            return null;
        }
        for (ResourceLocation texture : model.getTextures()) {
            event.getMap().registerSprite(texture);
        }
        return model;
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (SimpleMiningPipeModel miningPipe : MINING_PIPE_MODELS.values()) {
            if (miningPipe.unbakedBaseModel != null) {
                miningPipe.baseModel = miningPipe.unbakedBaseModel.bake(
                        miningPipe.unbakedBaseModel.getDefaultState(),
                        DefaultVertexFormats.ITEM,
                        ModelLoader.defaultTextureGetter());
                miningPipe.unbakedBaseModel = null;
            }
            if (miningPipe.unbakedBottomModel != null) {
                miningPipe.bottomModel = miningPipe.unbakedBottomModel.bake(
                        miningPipe.unbakedBottomModel.getDefaultState(),
                        DefaultVertexFormats.ITEM,
                        ModelLoader.defaultTextureGetter());
                miningPipe.unbakedBottomModel = null;
            }
        }
    }
}
