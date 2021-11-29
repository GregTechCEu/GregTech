package gregtech.api.capability.impl;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;

import javax.annotation.Nonnull;

/**
 * RecipeLogic for multiblocks that use temperature for raising speed and lowering energy usage
 * Used with RecipeMaps that run recipes using the {@link TemperatureProperty}
 */
public class HeatingCoilRecipeLogic extends MultiblockRecipeLogic {

    public HeatingCoilRecipeLogic(RecipeMapMultiblockController metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull Recipe recipe, boolean negativeEU, int maxOverclocks) {
        return heatingCoilOverclockingLogic(recipe.getEUt(), getMaxVoltage(), recipe.getDuration(), maxOverclocks,
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                recipe.getProperty(TemperatureProperty.getInstance(), 0));
    }

    @Nonnull
    public static int[] heatingCoilOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, int maxOverclocks, int currentTemp, int recipeRequiredTemp) {
        int amountEUDiscount = Math.max(0, (currentTemp - recipeRequiredTemp) / 900);
        int amountPerfectOC = amountEUDiscount / 2;

        // apply a multiplicative 95% energy multiplier for every 900k over recipe temperature
        recipeEUt *= Math.min(1, Math.pow(0.95, amountEUDiscount));

        // perfect overclock for every 1800k over recipe temperature
        if (amountPerfectOC > 0) {
            // use the normal overclock logic to do perfect OCs up to as many times as calculated
            int[] overclock = standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, PERFECT_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, amountPerfectOC);

            // overclock normally as much as possible after perfects are exhausted
            return standardOverclockingLogic(overclock[0], maximumVoltage, overclock[1], STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);
        }

        // no perfects are performed, do normal overclocking
        return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER, maxOverclocks);
    }
}
