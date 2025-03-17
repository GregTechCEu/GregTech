package gregtech.api.recipes.logic.statemachine.parallel;

import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.logic.statemachine.RecipeSearchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeViewOperator;
import gregtech.api.recipes.lookup.property.PowerCapacityProperty;
import gregtech.api.recipes.lookup.property.PowerSupplyProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.statemachine.GTStateMachineOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public class RecipeParallelLimitOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_LIMIT_KEY = "ParallelLimit";

    public static final Predicate<NBTTagCompound> SUCCESS_PREDICATE = t -> t.getInteger(STANDARD_LIMIT_KEY) > 0;

    protected final boolean canDownTransform;
    protected final String keyLimit;
    protected final String keyRecipeView;
    protected final String keyProperties;

    public RecipeParallelLimitOperator(boolean canDownTransform) {
        this.canDownTransform = canDownTransform;
        keyLimit = STANDARD_LIMIT_KEY;
        keyRecipeView = RecipeViewOperator.STANDARD_VIEW_KEY;
        keyProperties = RecipeSearchOperator.STANDARD_PROPERTIES_KEY;
    }

    public RecipeParallelLimitOperator(@NotNull IntSupplier limit, boolean canDownTransform, String keyLimit,
                                       String keyRecipeView, String keyProperties) {
        this.canDownTransform = canDownTransform;
        this.keyLimit = keyLimit;
        this.keyRecipeView = keyRecipeView;
        this.keyProperties = keyProperties;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        RecipeView recipe = (RecipeView) transientData.get(keyRecipeView);
        PropertySet properties = (PropertySet) transientData.get(keyProperties);
        if (recipe == null) throw new IllegalStateException();

        if (recipe.getActualEUt() != 0 && properties != null) {
            long amperage;
            long voltage;
            if (recipe.getRecipe().isGenerating()) {
                PowerCapacityProperty property = properties.getDefaultable(PowerCapacityProperty.EMPTY);
                amperage = property.amperage();
                voltage = property.voltage();
            } else {
                PowerSupplyProperty property = properties.getDefaultable(PowerSupplyProperty.EMPTY);
                amperage = property.amperage();
                voltage = property.voltage();
            }
            double voltageBoost = 1;
            if (canDownTransform) {
                voltageBoost = (double) voltage / recipe.getActualVoltage();
            }
            int limit = Math.min(data.getInteger(keyLimit),
                    (int) (voltageBoost * amperage / recipe.getActualAmperage()));
            data.setInteger(keyLimit, limit);
        }
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator limitSupplier(IntSupplier limit) {
        return limitSupplier(limit, RecipeParallelLimitOperator.STANDARD_LIMIT_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator limitSupplier(IntSupplier limit, String key) {
        return d -> d.setInteger(key, limit.getAsInt());
    }
}
