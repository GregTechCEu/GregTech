package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.RecipeRunner;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class MultiblockRecipeLogic extends DistributedRecipeLogic {

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity, tileEntity.recipeMap, false);
    }

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean initialDistributing) {
        super(tileEntity, tileEntity.recipeMap, initialDistributing);
    }

    @Override
    public @NotNull RecipeMapMultiblockController getMetaTileEntity() {
        return (RecipeMapMultiblockController) metaTileEntity;
    }

    @Override
    public void update() {}

    public void updateWorkable() {
        super.update();
    }

    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !getMetaTileEntity().isStructureObstructed();
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    @Override
    public void invalidate() {
        super.invalidate();
    }

    public IEnergyContainer getEnergyContainer() {
        return getMetaTileEntity().getEnergyContainer();
    }

    @Override
    public Collection<DistinctInputGroup> getInputGroups() {
        return getMetaTileEntity().getInputGroups();
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        return getMetaTileEntity().getOutputInventory();
    }

    @Override
    protected IMultipleTankHandler getOutputTank() {
        return getMetaTileEntity().getOutputFluidInventory();
    }

    @Override
    protected void findAndSetupRecipeToRun(@NotNull List<ItemStack> listViewOfItemInputs,
                                           @NotNull List<FluidStack> listViewOfFluidInputs,
                                           @NotNull PropertySet properties, @NotNull RecipeRunner runner) {
        // do not run recipes when there are more than 5 maintenance problems
        if (ConfigHolder.machines.enableMaintenance && getMetaTileEntity().hasMaintenanceMechanics() &&
                getMetaTileEntity().getNumMaintenanceProblems() > 5) {
            return;
        }
        super.findAndSetupRecipeToRun(listViewOfItemInputs, listViewOfFluidInputs, properties, runner);
    }

    @Override
    protected float computeDuration(RecipeView recipe, int overclocks) {
        MaintenanceValues values = getMaintenanceValues();
        if (recipe.getRecipe().isGenerating()) {
            return (float) (super.computeDuration(recipe, overclocks) / values.durationBonus() *
                    (1 - 0.1 * values.count()));
        } else {
            return (float) (super.computeDuration(recipe, overclocks) * values.durationBonus() *
                    (1 + 0.1 * values.count()));
        }
    }

    @Override
    protected boolean canSubtick() {
        return true;
    }

    @Override
    public long getMaxOverclockVoltage(boolean generatingRecipe) {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer instanceof EnergyContainerList) {
            long voltage;
            long amperage;
            if (energyContainer.getInputVoltage() > energyContainer.getOutputVoltage()) {
                voltage = energyContainer.getInputVoltage();
                amperage = energyContainer.getInputAmperage();
            } else {
                voltage = energyContainer.getOutputVoltage();
                amperage = energyContainer.getOutputAmperage();
            }

            if (amperage == 1) {
                // amperage is 1 when the energy is not exactly on a tier

                // the voltage for recipe search is always on tier, so take the closest lower tier
                return GTValues.V[GTUtility.getFloorTierByVoltage(voltage)];
            } else {
                // amperage != 1 means the voltage is exactly on a tier
                // ignore amperage, since only the voltage is relevant for recipe search
                // amps are never > 3 in an EnergyContainerList
                return voltage;
            }
        }
        return Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
    }

    @NotNull
    protected MaintenanceValues getMaintenanceValues() {
        MultiblockWithDisplayBase displayBase = getMetaTileEntity();
        int numMaintenanceProblems =
                !displayBase.hasMaintenanceMechanics() || !ConfigHolder.machines.enableMaintenance ? 0 : displayBase.getNumMaintenanceProblems();
        double durationMultiplier = 1.0D;
        if (displayBase.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
            durationMultiplier = displayBase.getMaintenanceDurationMultiplier();
        }
        return new MaintenanceValues(numMaintenanceProblems, durationMultiplier);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        RecipeMapMultiblockController controller = getMetaTileEntity();
        if (controller.checkRecipe(recipe, false)) {
            controller.checkRecipe(recipe, true);
            return super.checkRecipe(recipe);
        }
        return false;
    }

    @Override
    protected void attemptRecipeCompletion(RecipeRunner runner) {
        super.attemptRecipeCompletion(runner);
        RecipeRun run = runner.getCurrent();
        if (run == null) return;
        performMufflerOperations(run);
    }

    protected void performMufflerOperations(@NotNull RecipeRun run) {
        RecipeMapMultiblockController controller = getMetaTileEntity();
        // output muffler items
        if (controller.hasMufflerMechanics()) {
            controller.outputRecoveryItems(run.getParallel());
        }
    }

    protected long getEnergyStored() {
        return getEnergyContainer().getEnergyStored();
    }

    protected long getEnergyCapacity() {
        return getEnergyContainer().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) getEnergyContainer().changeEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    protected boolean produceEnergy(long eu, boolean simulate) {
        long resultEnergy = getEnergyStored() + eu;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) getEnergyContainer().changeEnergy(eu);
            return true;
        } else return false;
    }

    @Override
    public long getMaxVoltageIn() {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer instanceof EnergyContainerList energyList) {
            long highestVoltage = energyList.getHighestInputVoltage();
            if (energyList.getNumHighestInputContainers() > 1) {
                // allow tier + 1 if there are multiple hatches present at the highest tier
                int tier = GTUtility.getTierByVoltage(highestVoltage);
                return GTValues.V[Math.min(tier + 1, GTValues.MAX)];
            } else {
                return highestVoltage;
            }
        } else {
            return energyContainer.getInputVoltage();
        }
    }

    @Override
    public long getMaxVoltageOut() {
        IEnergyContainer energyContainer = getEnergyContainer();
        // Generators
        long voltage = energyContainer.getOutputVoltage();
        long amperage = energyContainer.getOutputAmperage();
        if (energyContainer instanceof EnergyContainerList && amperage == 1) {
            // Amperage is 1 when the energy is not exactly on a tier.
            // The voltage for recipe search is always on tier, so take the closest lower tier.
            // List check is done because single hatches will always be a "clean voltage," no need
            // for any additional checks.
            return GTValues.V[GTUtility.getFloorTierByVoltage(voltage)];
        }
        return voltage;
    }

    @Override
    public long getMaxAmperageIn() {
        return getEnergyContainer().getInputAmperage();
    }

    @Override
    public long getMaxAmperageOut() {
        return getEnergyContainer().getOutputAmperage();
    }

    @Nullable
    @Override
    public RecipeMap<?> getRecipeMap() {
        // if the multiblock has more than one RecipeMap, return the currently selected one
        if (metaTileEntity instanceof IMultipleRecipeMaps)
            return ((IMultipleRecipeMaps) metaTileEntity).getCurrentRecipeMap();
        return super.getRecipeMap();
    }

    protected record MaintenanceValues(int count, double durationBonus) {}
}
