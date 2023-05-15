package gregtech.api.capability.impl;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.util.GTUtility;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.recipes.logic.OverclockingLogic.heatingCoilOverclockingLogic;

/**
 * RecipeLogic for multiblocks that use temperature for raising speed and lowering energy usage
 * Used with RecipeMaps that run recipes using the {@link TemperatureProperty}
 */
public class HeatingCoilRecipeLogic extends MultiblockRecipeLogic {

    public HeatingCoilRecipeLogic(RecipeMapMultiblockController metaTileEntity) {
        super(metaTileEntity);
        if (!(metaTileEntity instanceof IHeatingCoil)) {
            throw new IllegalArgumentException("MetaTileEntity must be instanceof IHeatingCoil");
        }
    }

    @Override
    protected int[] performOverclocking(@Nonnull Recipe recipe) {
        // mostly duplicated from AbstractRecipeLogic#performOverclocking(Recipe)
        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());

        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        // cannot overclock, so return the starting values, but with the EU/t discount
        if (numberOfOCs <= 0) {
            int requiredTemp = recipe.getProperty(TemperatureProperty.getInstance(), 0);
            if (requiredTemp < OverclockingLogic.COIL_EUT_DISCOUNT_TEMPERATURE) {
                return new int[]{recipe.getEUt(), recipe.getDuration()};
            }

            int currentTemp = ((IHeatingCoil) metaTileEntity).getCurrentTemperature();
            int amountEUtDiscount = OverclockingLogic.calculateAmountCoilEUtDiscount(currentTemp, requiredTemp);
            int discounted = OverclockingLogic.applyCoilEUtDiscount(recipe.getEUt(), amountEUtDiscount);
            return new int[]{discounted, recipe.getDuration()};
        }

        return runOverclockingLogic(recipe.getRecipePropertyStorage(), recipe.getEUt(), getMaximumOverclockVoltage(), recipe.getDuration(), numberOfOCs);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int duration, int amountOC) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        return heatingCoilOverclockingLogic(
                Math.abs(recipeEUt),
                maxVoltage,
                (int) Math.round(duration * maintenanceValues.getSecond()),
                amountOC,
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                propertyStorage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0)
        );
    }
}
