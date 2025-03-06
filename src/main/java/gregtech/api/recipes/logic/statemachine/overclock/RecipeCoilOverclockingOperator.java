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
import gregtech.api.recipes.lookup.property.TemperatureMaximumProperty;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.statemachine.GTStateMachineTransientOperator;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.DoubleSupplier;

public class RecipeCoilOverclockingOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_RESULT_KEY = "SelectedRun";

    protected final double costFactor;
    protected final double speedFactor;
    protected final boolean canUpTransform;
    protected final DoubleSupplier durationDiscount;
    protected final String keyView;
    protected final String keyProperties;
    protected final String keyRun;

    public RecipeCoilOverclockingOperator(double costFactor, double speedFactor, boolean canUpTransform,
                                          @Nullable DoubleSupplier durationDiscount) {
        this.costFactor = costFactor;
        this.speedFactor = speedFactor;
        this.canUpTransform = canUpTransform;
        this.durationDiscount = durationDiscount;
        keyView = RecipeViewOperator.STANDARD_VIEW_KEY;
        keyProperties = RecipeSearchOperator.STANDARD_PROPERTIES_KEY;
        keyRun = STANDARD_RESULT_KEY;
    }

    public RecipeCoilOverclockingOperator(double costFactor, double speedFactor, boolean canUpTransform,
                                          DoubleSupplier durationDiscount, String keyView, String keyProperties,
                                          String keyRun) {
        this.costFactor = costFactor;
        this.speedFactor = speedFactor;
        this.canUpTransform = canUpTransform;
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

        int machineTemp = properties.getDefaultable(TemperatureMaximumProperty.EMPTY).temperature();
        int recipeTemp = view.getRecipe().getProperty(TemperatureProperty.getInstance(), 0);

        if (recipeTemp > machineTemp) throw new IllegalStateException();

        int discounts = (recipeTemp - machineTemp) / RecipeLogicConstants.COIL_VOLTAGE_DISCOUNT_TEMPERATURE;
        double heatDiscount = Math.pow(RecipeLogicConstants.COIL_VOLTAGE_DISCOUNT_FACTOR, discounts);
        int perfects = (recipeTemp - machineTemp) / RecipeLogicConstants.COIL_PERFECT_OVERCLOCK_TEMPERATURE;

        int recipeVoltageTier = GTUtility.getTierByVoltage(view.getRecipe().getVoltage());
        long machineVoltage;
        long amperage;
        if (view.getRecipe().isGenerating()) {
            PowerCapacityProperty property = properties.getDefaultable(PowerCapacityProperty.EMPTY);
            machineVoltage = property.voltage();
            amperage = property.amperage();
        } else {
            PowerSupplyProperty property = properties.getDefaultable(PowerSupplyProperty.EMPTY);
            machineVoltage = property.voltage();
            amperage = property.amperage();
        }
        int machineVoltageTier = GTUtility.getFloorTierByVoltage(machineVoltage);
        int overclocks;
        if (canUpTransform) {
            // log base cost factor of the ratio of available EUt to required EUt
            overclocks = (int) (Math.log((double) (machineVoltage * amperage) / (view.getActualEUt() * heatDiscount)) /
                    Math.log(costFactor));
        } else {
            overclocks = machineVoltageTier - recipeVoltageTier;
            while (view.getActualEUt() * heatDiscount * Math.pow(costFactor, overclocks) > machineVoltage * amperage)
                overclocks--;
        }
        overclocks = Math.max(0, overclocks);

        double factor = Math.pow(RecipeLogicConstants.PERFECT_OVERCLOCK_SPEED_FACTOR, Math.min(perfects, overclocks)) *
                Math.pow(costFactor, Math.max(0, overclocks - perfects));
        transientData.put(keyRun, new SingleRecipeRun(view, recipeVoltageTier, machineVoltageTier, properties,
                Math.pow(costFactor, overclocks), view.getActualDuration() * discount / factor));
    }
}
