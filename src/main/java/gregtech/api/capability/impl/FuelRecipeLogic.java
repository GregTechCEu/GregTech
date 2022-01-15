package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class FuelRecipeLogic extends RecipeLogicEnergy {

    public FuelRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull Recipe recipe, boolean negativeEU, int maxOverclocks) {
        // no overclocking happens other than parallelization,
        // so return the recipe's values, with EUt made positive for it to be made negative later
        return new int[]{recipe.getEUt() * -1, recipe.getDuration()};
    }

    @Override
    public Enum<ParallelLogicType> getParallelLogicType() {
        return ParallelLogicType.MULTIPLY; //TODO APPEND_FLUIDS
    }

    @Override
    protected boolean hasEnoughPower(@Nonnull int[] resultOverclock) {
        // generators always have enough power to run recipes
        return true;
    }

    @Override
    public void applyParallelBonus(@Nonnull RecipeBuilder<?> builder) {
        // the builder automatically multiplies by -1, so nothing extra is needed here
        builder.EUt(builder.getEUt());
    }

    @Override
    public int getParallelLimit() {
        return (int) Math.max(1, Math.pow(4, getOverclockTier() - 1));
    }
}
