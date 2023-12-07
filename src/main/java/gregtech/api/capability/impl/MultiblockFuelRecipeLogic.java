package gregtech.api.capability.impl;

import gregtech.api.capability.IRotorHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiblockFuelRecipeLogic extends MultiblockRecipeLogic {

    protected long totalContinuousRunningTime;

    public MultiblockFuelRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    protected void modifyOverclockPre(@NotNull int[] values, @NotNull IRecipePropertyStorage storage) {
        // apply maintenance bonuses
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration bonus
        if (maintenanceValues.getSecond() != 1.0) {
            values[1] = (int) Math.round(values[1] / maintenanceValues.getSecond());
        }
    }

    @Override
    protected void modifyOverclockPost(int[] overclockResults, @NotNull IRecipePropertyStorage storage) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration penalty
        if (maintenanceValues.getFirst() > 0) {
            overclockResults[1] = (int) (overclockResults[1] * (1 - 0.1 * maintenanceValues.getFirst()));
        }

        // make EUt negative so it is consumed
        overclockResults[0] = -overclockResults[0];
    }

    @NotNull
    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY; // TODO APPEND_FLUIDS
    }

    @Override
    protected boolean hasEnoughPower(@NotNull int[] resultOverclock) {
        // generators always have enough power to run recipes
        return true;
    }

    @Override
    public void update() {
        super.update();
        if (workingEnabled && isActive && progressTime > 0) {
            totalContinuousRunningTime++;
        } else {
            totalContinuousRunningTime = 0;
        }
    }

    @Override
    public int getParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }

    /**
     * Boost the energy production.
     * Should not change the state of the workable logic. Only read current values.
     *
     * @param production the energy amount to boost
     * @return the boosted energy amount
     */
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
    public int getInfoProviderEUt() {
        return (int) boostProduction(super.getInfoProviderEUt());
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        totalContinuousRunningTime = 0;
    }

    public String getRecipeFluidInputInfo() {
        IRotorHolder rotorHolder = null;

        if (metaTileEntity instanceof MultiblockWithDisplayBase multiblockWithDisplayBase) {
            List<IRotorHolder> abilities = multiblockWithDisplayBase.getAbilities(MultiblockAbility.ROTOR_HOLDER);
            rotorHolder = abilities.size() > 0 ? abilities.get(0) : null;
        }

        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        Recipe recipe;
        if (previousRecipe == null) {
            recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());
            if (recipe == null) return null;
        } else {
            recipe = previousRecipe;
        }
        FluidStack requiredFluidInput = recipe.getFluidInputs().get(0).getInputFluidStack();

        int ocAmount = (int) (getMaxVoltage() / recipe.getEUt());
        int neededAmount = ocAmount * requiredFluidInput.amount;
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            neededAmount /= (rotorHolder.getTotalEfficiency() / 100f);
        } else if (rotorHolder != null && !rotorHolder.hasRotor()) {
            return null;
        }
        return TextFormatting.RED + TextFormattingUtil.formatNumbers(neededAmount) + "L";
    }

    public FluidStack getInputFluidStack() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        if (previousRecipe == null) {
            Recipe recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());

            return recipe == null ? null : getInputTank().drain(
                    new FluidStack(recipe.getFluidInputs().get(0).getInputFluidStack().getFluid(), Integer.MAX_VALUE),
                    false);
        }
        FluidStack fuelStack = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
        return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    @Override
    public boolean isAllowOverclocking() {
        return false;
    }
}
