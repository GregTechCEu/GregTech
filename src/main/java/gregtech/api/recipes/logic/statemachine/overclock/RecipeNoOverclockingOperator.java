package gregtech.api.recipes.logic.statemachine.overclock;

import gregtech.api.recipes.logic.PrimitiveRecipeRun;
import gregtech.api.recipes.logic.RecipeLogicConstants;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.logic.SingleRecipeRun;
import gregtech.api.recipes.logic.statemachine.RecipeMaintenanceOperator;
import gregtech.api.recipes.logic.statemachine.RecipeSearchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeViewOperator;
import gregtech.api.recipes.lookup.property.PowerCapacityProperty;
import gregtech.api.recipes.lookup.property.PowerSupplyProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.statemachine.GTStateMachineTransientOperator;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.DoubleSupplier;

public class RecipeNoOverclockingOperator implements GTStateMachineTransientOperator {

    protected final @Nullable DoubleSupplier durationDiscount;
    protected final String keyView;
    protected final String keyProperties;
    protected final String keyRun;

    public static RecipeNoOverclockingOperator create(double costFactor, double speedFactor, boolean canUpTransform,
                                                      @Nullable DoubleSupplier durationDiscount) {
        return new RecipeNoOverclockingOperator(durationDiscount);
    }

    public RecipeNoOverclockingOperator(@Nullable DoubleSupplier durationDiscount) {
        this.durationDiscount = durationDiscount;
        keyView = RecipeViewOperator.STANDARD_VIEW_KEY;
        keyProperties = RecipeSearchOperator.STANDARD_PROPERTIES_KEY;
        keyRun = RecipeStandardOverclockingOperator.STANDARD_RESULT_KEY;
    }

    public RecipeNoOverclockingOperator(@Nullable DoubleSupplier durationDiscount, String keyView, String keyProperties,
                                        String keyRun) {
        this.durationDiscount = durationDiscount;
        this.keyView = keyView;
        this.keyProperties = keyProperties;
        this.keyRun = keyRun;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        RecipeView view = (RecipeView) transientData.get(keyView);
        PropertySet properties = (PropertySet) transientData.get(keyProperties);
        RecipeMaintenanceOperator.MaintenanceValues maintenance = (RecipeMaintenanceOperator.MaintenanceValues) transientData
                .get(RecipeMaintenanceOperator.STANDARD_KEY);
        if (view == null || properties == null) throw new IllegalStateException();

        double discount = durationDiscount != null ? durationDiscount.getAsDouble() : 1;
        if (maintenance != null) {
            discount *= maintenance.durationBonus();
            if (view.getRecipe().isGenerating()) {
                discount *= 1 - RecipeLogicConstants.MAINTENANCE_PROBLEM_DURATION_FACTOR * maintenance.count();
            } else {
                discount *= 1 + RecipeLogicConstants.MAINTENANCE_PROBLEM_DURATION_FACTOR * maintenance.count();
            }
        }
        if (view.getActualEUt() == 0) {
            transientData.put(keyRun, new PrimitiveRecipeRun(view, properties, view.getActualDuration() * discount));
            return;
        }
        int recipeVoltageTier = GTUtility.getTierByVoltage(view.getRecipe().getVoltage());
        long machineVoltage;
        if (view.getRecipe().isGenerating()) {
            PowerCapacityProperty property = properties.getDefaultable(PowerCapacityProperty.EMPTY);
            machineVoltage = property.voltage();
        } else {
            PowerSupplyProperty property = properties.getDefaultable(PowerSupplyProperty.EMPTY);
            machineVoltage = property.voltage();
        }
        int machineVoltageTier = GTUtility.getFloorTierByVoltage(machineVoltage);
        transientData.put(keyRun, new SingleRecipeRun(view, recipeVoltageTier, machineVoltageTier, properties, 1,
                view.getActualDuration() * discount));
    }
}
