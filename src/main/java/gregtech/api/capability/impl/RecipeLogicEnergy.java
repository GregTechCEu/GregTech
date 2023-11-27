package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;

import java.util.function.Supplier;

public class RecipeLogicEnergy extends AbstractRecipeLogic {

    protected final Supplier<IEnergyContainer> energyContainer;

    public RecipeLogicEnergy(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                             Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap);
        this.energyContainer = energyContainer;
        setMaximumOverclockVoltage(getMaxVoltage());
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return energyContainer.get().getInputPerSec();
    }

    @Override
    protected long getEnergyStored() {
        return energyContainer.get().getEnergyStored();
    }

    @Override
    protected long getEnergyCapacity() {
        return energyContainer.get().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) energyContainer.get().changeEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    public long getMaxVoltage() {
        return Math.max(energyContainer.get().getInputVoltage(),
                energyContainer.get().getOutputVoltage());
    }
}
