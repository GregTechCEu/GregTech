package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import gregtech.api.util.GTUtility;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static gregtech.api.recipes.logic.OverclockingLogic.subTickNonParallelOC;

/**
 * For singleblocks.
 */
public class RecipeLogicEnergy extends AbstractRecipeLogic {

    protected final Supplier<IEnergyContainer> energyContainer;

    protected long overclockVoltage;

    public RecipeLogicEnergy(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                             Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap);
        this.energyContainer = energyContainer;
    }

    public void setOverclockTier(int tier) {
        overclockVoltage = GTValues.V[tier];
    }

    @Override
    public long getMaxOverclockVoltage(boolean generatingRecipe) {
        return overclockVoltage;
    }

    protected long getEnergyStored() {
        return energyContainer.get().getEnergyStored();
    }

    protected long getEnergyCapacity() {
        return energyContainer.get().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) energyContainer.get().changeEnergy(-recipeEUt);
            return true;
        }
        return false;
    }

    @Override
    protected boolean produceEnergy(long eu, boolean simulate) {
        long resultEnergy = getEnergyStored() + eu;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) energyContainer.get().changeEnergy(eu);
            return true;
        } else return false;
    }

    @Override
    public long getMaxVoltageIn() {
        return energyContainer.get().getInputVoltage();
    }

    @Override
    public long getMaxVoltageOut() {
        return energyContainer.get().getOutputVoltage();
    }

    @Override
    public long getMaxAmperageIn() {
        return energyContainer.get().getInputAmperage();
    }

    @Override
    public long getMaxAmperageOut() {
        return energyContainer.get().getOutputAmperage();
    }

    @Override
    protected boolean canSubtick() {
        return false;
    }
}
