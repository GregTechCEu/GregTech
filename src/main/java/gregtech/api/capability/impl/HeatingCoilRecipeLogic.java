package gregtech.api.capability.impl;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.OverclockingConstants;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.TemperatureMaximumProperty;
import gregtech.api.recipes.properties.impl.TemperatureProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * RecipeLogic for multiblocks that use temperature for raising speed and lowering energy usage
 * Used with RecipeMaps that run recipes using the {@link TemperatureProperty}
 */
public class HeatingCoilRecipeLogic extends MultiblockRecipeLogic {

    public <T extends RecipeMapMultiblockController & IHeatingCoil> HeatingCoilRecipeLogic(T metaTileEntity) {
        super(metaTileEntity);
    }

    public IHeatingCoil getCoil() {
        return (IHeatingCoil) metaTileEntity;
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.add(new TemperatureMaximumProperty(getCoil().getCurrentTemperature()));
        return set;
    }

    @Override
    protected float computeVoltageMultiplier(RecipeView recipe,
                                             @Range(from = 0, to = Integer.MAX_VALUE) int overclocks) {
        int discounts = calculateAmountCoilEUtDiscount(getCoil().getCurrentTemperature(),
                recipe.getRecipe().getProperty(TemperatureProperty.getInstance(), 0));
        if (discounts == 0) return super.computeVoltageMultiplier(recipe, overclocks);
        else return (float) (super.computeVoltageMultiplier(recipe, overclocks) * Math.pow(0.95, discounts));
    }

    @Override
    protected float computeDurationMultiplier(RecipeView recipe,
                                              @Range(from = 0, to = Integer.MAX_VALUE) int overclocks) {
        int perfects = Math.min(overclocks, calculateAmountCoilPerfectOverclocks(getCoil().getCurrentTemperature(),
                recipe.getRecipe().getProperty(TemperatureProperty.getInstance(), 0)));
        return (float) (Math.pow(OverclockingConstants.PERFECT_DURATION_FACTOR, perfects) *
                Math.pow(getOverclockingDurationFactor(), overclocks - perfects));
    }

    protected @Range(from = 0, to = Integer.MAX_VALUE) int calculateAmountCoilEUtDiscount(int providedTemp,
                                                                                          int requiredTemp) {
        return Math.max(0, (providedTemp - requiredTemp) / OverclockingConstants.COIL_EUT_DISCOUNT_TEMPERATURE);
    }

    protected @Range(from = 0, to = Integer.MAX_VALUE) int calculateAmountCoilPerfectOverclocks(int providedTemp,
                                                                                                int requiredTemp) {
        return Math.max(0, (providedTemp - requiredTemp) / OverclockingConstants.COIL_PERFECT_OVERCLOCK_TEMPERATURE);
    }
}
