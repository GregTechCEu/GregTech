package gregtech.api.capability.impl;

import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;

public class MultiblockFuelRecipeLogic extends MultiblockRecipeLogic {

    protected long totalContinuousRunningTime;

    public MultiblockFuelRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Nonnull
    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int amountOC) {
        // no overclocking happens other than parallelization,
        // so return the recipe's values, with EUt made positive for it to be made negative later
        return new int[]{-recipeEUt, recipeDuration};
    }

    @Override
    protected void modifyOverclockPre(@Nonnull int[] values, @Nonnull IRecipePropertyStorage storage) {
        // apply maintenance bonuses
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration bonus
        if (maintenanceValues.getSecond() != 1.0) {
            values[1] = (int) Math.round(values[1] / maintenanceValues.getSecond());
        }
    }

    @Override
    protected void modifyOverclockPost(int[] overclockResults, @Nonnull IRecipePropertyStorage storage) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration penalty
        if (maintenanceValues.getFirst() > 0) {
            overclockResults[1] = (int) (overclockResults[1] * (1 - 0.1 * maintenanceValues.getFirst()));
        }
    }

    @Nonnull
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
    public void update() {
        super.update();
        if (workingEnabled && isActive && progressTime > 0) {
                totalContinuousRunningTime ++;
        } else {
            totalContinuousRunningTime = 0;
        }
    }

    @Override
    public int getParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }

    protected long boostProduction(long production) {
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
