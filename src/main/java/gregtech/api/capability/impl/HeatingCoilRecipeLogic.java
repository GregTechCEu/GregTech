package gregtech.api.capability.impl;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;

import org.jetbrains.annotations.NotNull;

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
    protected void modifyOverclockPre(@NotNull int[] values, @NotNull IRecipePropertyStorage storage) {
        super.modifyOverclockPre(values, storage);
        // coil EU/t discount
        values[0] = OverclockingLogic.applyCoilEUtDiscount(values[0],
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                storage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0));
    }

    @NotNull
    @Override
    protected int[] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt,
                                         long maxVoltage, int duration, int amountOC) {
        return heatingCoilOverclockingLogic(
                Math.abs(recipeEUt),
                maxVoltage,
                duration,
                amountOC,
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                propertyStorage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0));
    }
}
