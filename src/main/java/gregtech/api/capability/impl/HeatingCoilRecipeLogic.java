package gregtech.api.capability.impl;

import gregtech.api.capability.IHeatingCoil;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.logic.OverclockingLogic.heatingCoilOC;

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
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull IRecipePropertyStorage storage) {
        super.modifyOverclockPre(ocParams, storage);
        // coil EU/t discount
        ocParams.setEut(OverclockingLogic.applyCoilEUtDiscount(ocParams.eut(),
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                storage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0)));
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull IRecipePropertyStorage propertyStorage, long maxVoltage) {
        heatingCoilOC(ocParams, ocResult, maxVoltage, ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                propertyStorage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0));
    }
}
