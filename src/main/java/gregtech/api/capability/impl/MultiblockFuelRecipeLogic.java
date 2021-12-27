package gregtech.api.capability.impl;

import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;

import javax.annotation.Nonnull;

public class MultiblockFuelRecipeLogic extends MultiblockRecipeLogic {

    protected int totalContinuousRunningTime;

    public MultiblockFuelRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull Recipe recipe, boolean negativeEU, int maxOverclocks) {
        // apply maintenance penalties
        MultiblockWithDisplayBase displayBase = this.metaTileEntity instanceof MultiblockWithDisplayBase ? (MultiblockWithDisplayBase) metaTileEntity : null;
        int numMaintenanceProblems = displayBase == null ? 0 : displayBase.getNumMaintenanceProblems();

        int[] overclock = null;
        if (displayBase != null && displayBase.hasMaintenanceMechanics()) {
            IMaintenanceHatch hatch = displayBase.getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
            double durationMultiplier = hatch.getDurationMultiplier();
            if (durationMultiplier != 1.0) {
                overclock = new int[]{recipe.getEUt() * -1, (int) Math.round(recipe.getDuration() / durationMultiplier)};
            }
        }
        if (overclock == null) overclock = new int[]{recipe.getEUt() * -1, recipe.getDuration()};
        overclock[1] = (int) (overclock[1] * (1 - 0.1 * numMaintenanceProblems));

        // no overclocking happens other than parallelization,
        // so return the recipe's values, with EUt made positive for it to be made negative later
        return overclock;
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
        return boostConsumption((int) (Math.max(1, Math.pow(4, getOverclockTier() - 1))));
    }

    @Override
    public void update() {
        super.update();
        if (workingEnabled && progressTime > 0) {
            if (totalContinuousRunningTime == Integer.MAX_VALUE)
                totalContinuousRunningTime = 0;
            else
                totalContinuousRunningTime += 1;
        } else {
            totalContinuousRunningTime = 0;
        }
    }

    protected int boostConsumption(int consumption) {
        return consumption;
    }

    protected long boostProduction(int production) {
        return production;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        long euToDraw = boostProduction(recipeEUt);
        long resultEnergy = getEnergyStored() - euToDraw;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) getEnergyContainer().changeEnergy(-euToDraw);
            return true;
        } else return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        totalContinuousRunningTime = 0;
    }
}
