package gregtech.api.capability.impl;

import gregtech.api.capability.IRotorHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

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

    public String[] getRecipeFluidInputAmount() {
        IRotorHolder rotorHolder = null;

        if (metaTileEntity instanceof MultiblockWithDisplayBase multiblockWithDisplayBase) {
            List<IRotorHolder> abilities = multiblockWithDisplayBase.getAbilities(MultiblockAbility.ROTOR_HOLDER);
            rotorHolder = abilities.size() > 0 ? abilities.get(0) : null;
        }

        FluidStack requiredFluidInput = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
        String neededName = requiredFluidInput.getLocalizedName();

        int ocAmount = (int) (getMaxVoltage() / -previousRecipe.getEUt());
        int neededAmount = ocAmount * requiredFluidInput.amount;
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            neededAmount /= (rotorHolder.getTotalEfficiency() / 100f);
        }
        return new String[] {
                TextFormatting.RED + TextFormattingUtil.formatNumbers(neededAmount) + "L",
                neededName
        };
    }
}
