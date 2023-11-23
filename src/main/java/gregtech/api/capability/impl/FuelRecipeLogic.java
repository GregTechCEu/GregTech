package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class FuelRecipeLogic extends RecipeLogicEnergy {

    public FuelRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @Nonnull
    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY; //TODO APPEND_FLUIDS
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    protected boolean hasEnoughPower(@Nonnull int[] resultOverclock) {
        // generators always have enough power to run recipes
        return true;
    }

    @Override
    protected void modifyOverclockPost(int[] overclockResults, @Nonnull IRecipePropertyStorage storage) {
        super.modifyOverclockPost(overclockResults, storage);
        // make EUt negative so it is consumed
        overclockResults[0] = -overclockResults[0];
    }

    @Override
    public int getParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }
}
