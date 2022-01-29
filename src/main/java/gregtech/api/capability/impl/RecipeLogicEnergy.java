package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.CleanroomProperty;

import java.util.function.Supplier;

public class RecipeLogicEnergy extends AbstractRecipeLogic {

    protected final Supplier<IEnergyContainer> energyContainer;

    public RecipeLogicEnergy(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
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
    protected long getMaxVoltage() {
        return Math.max(energyContainer.get().getInputVoltage(),
                energyContainer.get().getOutputVoltage());
    }

    @Override
    protected boolean checkRecipe(Recipe recipe) {
        if (!super.checkRecipe(recipe))
            return false;

        CleanroomType requiredType = recipe.getProperty(CleanroomProperty.getInstance(), null);
        if (requiredType == null)
            return true;

        ICleanroomProvider cleanroomProvider = ((WorkableTieredMetaTileEntity) getMetaTileEntity()).getCleanroom();
        if (cleanroomProvider == null)
            return false;

        return cleanroomProvider.isClean() && requiredType == cleanroomProvider.getType();
    }
}
