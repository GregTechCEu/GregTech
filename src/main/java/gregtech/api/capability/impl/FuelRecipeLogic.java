package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOC;

public class FuelRecipeLogic extends RecipeLogicEnergy {

    public FuelRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                           Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @NotNull
    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY; // TODO APPEND_FLUIDS
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        // generators always have enough power to run recipes
        return true;
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        standardOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(), getOverclockingVoltageFactor());
    }

    @Override
    public int getParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAllowOverclocking() {
        return false;
    }

    // generators always run recipes
    @Override
    protected void updateRecipeProgress() {
        if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
            drawEnergy(recipeEUt, false);
            // as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        }
    }
}
