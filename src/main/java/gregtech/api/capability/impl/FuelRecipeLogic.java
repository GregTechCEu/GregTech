package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * For singleblocks.
 */
public class FuelRecipeLogic extends RecipeLogicEnergy {

    public FuelRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                           Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @Override
    public long getMaxOverclockVoltage(boolean generatingRecipe) {
        return 0;
    }

    @Override
    public int getParallelLimit(@Nullable Recipe recipe) {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }
}
