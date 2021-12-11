package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class FuelRecipeLogic2 extends RecipeLogicEnergy {

    public FuelRecipeLogic2(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @Override
    protected void updateRecipeProgress() {
        boolean drawEnergy = drawEnergy(recipeEUt);
        if (drawEnergy) {
            //as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
            if (this.hasNotEnoughEnergy && getEnergyInputPerSecond() > 19L * recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (recipeEUt > 0) {
            //only set hasNotEnoughEnergy if this recipe is consuming recipe
            //generators always have enough energy
            this.hasNotEnoughEnergy = true;
            //if current progress value is greater than 2, decrement it by 2
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy) {
                    this.progressTime = 1;
                } else {
                    this.progressTime = Math.max(1, progressTime - 2);
                }
            }
        }
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
}
