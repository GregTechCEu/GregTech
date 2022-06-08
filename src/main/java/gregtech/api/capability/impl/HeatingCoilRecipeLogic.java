package gregtech.api.capability.impl;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.recipeproperties.RecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;

import javax.annotation.Nonnull;

import static gregtech.api.recipes.logic.OverclockingLogic.heatingCoilOverclockingLogic;

/**
 * RecipeLogic for multiblocks that use temperature for raising speed and lowering energy usage
 * Used with RecipeMaps that run recipes using the {@link TemperatureProperty}
 */
public class HeatingCoilRecipeLogic extends MultiblockRecipeLogic {

    public HeatingCoilRecipeLogic(RecipeMapMultiblockController metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull RecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int duration, int maxOverclocks) {
        return heatingCoilOverclockingLogic(Math.abs(recipeEUt),
                maxVoltage,
                duration,
                maxOverclocks,
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                propertyStorage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0)
        );
    }


}
