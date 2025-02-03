package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static gregtech.api.recipes.logic.OverclockingLogic.subTickNonParallelOC;

public class RecipeLogicEnergy extends AbstractRecipeLogic {

    protected final Supplier<IEnergyContainer> energyContainer;

    public RecipeLogicEnergy(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                             Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap);
        this.energyContainer = energyContainer;
        setMaximumOverclockVoltage(getMaxVoltage());
    }

    @Override
    protected IEnergyContainer getEnergyContainer() {
        return energyContainer.get();
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        subTickNonParallelOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(),
                getOverclockingVoltageFactor());
    }
}
