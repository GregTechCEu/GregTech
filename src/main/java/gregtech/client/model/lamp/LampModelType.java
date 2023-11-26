package gregtech.client.model.lamp;

import gregtech.api.util.GTUtility;
import gregtech.client.model.BorderlessLampBakedModel;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

/**
 * Type of the lamp model. Provides name for resolving and generating model names,
 * as well as a method to build the {@link IBakedModel} instance.
 *
 * @see LampBakedModel
 */
public abstract class LampModelType {

    public static final LampModelType LAMP = new LampModelType(GTUtility.gregtechId("lamp")) {

        @Override
        public IBakedModel createModel(ModelResourceLocation modelLocation) {
            return new LampBakedModel(modelLocation);
        }

        @Override
        public IBakedModel createModel(IBakedModel model) {
            return new LampBakedModel(model);
        }
    };

    public static final LampModelType BORDERLESS_LAMP = new LampModelType(GTUtility.gregtechId("lamp_borderless")) {

        @Override
        public IBakedModel createModel(ModelResourceLocation modelLocation) {
            return new BorderlessLampBakedModel(modelLocation);
        }

        @Override
        public IBakedModel createModel(IBakedModel model) {
            return new BorderlessLampBakedModel(model);
        }
    };

    public final ResourceLocation modelName;

    public LampModelType(ResourceLocation modelName) {
        this.modelName = Objects.requireNonNull(modelName);
    }

    /**
     * Create a {@link IBakedModel} instance for active lamp block, with ID of
     * the original model. In some cases the original model should be resolved
     * after registration, due to some other mod (i.e. CTM) swapping out the
     * instance after we could modify the registry.
     *
     * @param modelLocation ID of the original model
     * @return A {@link IBakedModel} instance for active lamp block
     */
    public abstract IBakedModel createModel(ModelResourceLocation modelLocation);

    /**
     * Create a {@link IBakedModel} instance for active lamp item, with the
     * original model.
     *
     * @param model Original model
     * @return A {@link IBakedModel} instance for active lamp item
     */
    public abstract IBakedModel createModel(IBakedModel model);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LampModelType that = (LampModelType) o;
        return modelName.equals(that.modelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName);
    }

    @Override
    public String toString() {
        return "LampModelType{" +
                "modelName='" + modelName + '\'' +
                '}';
    }
}
