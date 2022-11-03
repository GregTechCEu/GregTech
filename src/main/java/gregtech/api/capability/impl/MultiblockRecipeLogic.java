package gregtech.api.capability.impl;

import gregtech.api.capability.*;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.common.ConfigHolder;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;

public class MultiblockRecipeLogic extends AbstractRecipeLogic {

    // Used for distinct mode
    protected int lastRecipeIndex = 0;
    protected IItemHandlerModifiable currentDistinctInputBus;
    protected List<IItemHandlerModifiable> invalidatedInputList = new ArrayList<>();

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity, tileEntity.recipeMap);
    }

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, tileEntity.recipeMap, hasPerfectOC);
    }

    @Override
    public void update() {
    }

    public void updateWorkable() {
        super.update();
    }

    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !((IMultiblockController) metaTileEntity).isStructureObstructed();
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    public void invalidate() {
        previousRecipe = null;
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        lastRecipeIndex = 0;
        parallelRecipesPerformed = 0;
        isOutputsFull = false;
        invalidInputsForRecipes = false;
        invalidatedInputList.clear();
        setActive(false); // this marks dirty for us
    }

    public void onDistinctChanged() {
        this.lastRecipeIndex = 0;
    }

    public IEnergyContainer getEnergyContainer() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getEnergyContainer();
    }

    @Override
    protected IItemHandlerModifiable getInputInventory() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getInputInventory();
    }

    // Used for distinct bus recipe checking
    protected List<IItemHandlerModifiable> getInputBuses() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getAbilities(MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getOutputInventory();
    }

    @Override
    protected IMultipleTankHandler getInputTank() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getInputFluidInventory();
    }

    @Override
    protected IMultipleTankHandler getOutputTank() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getOutputFluidInventory();
    }

    @Override
    protected boolean canWorkWithInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (controller instanceof RecipeMapMultiblockController) {
            RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;

            if (distinctController.canBeDistinct() && distinctController.isDistinct()) {
                boolean canWork = false;
                if (invalidatedInputList.isEmpty()) {
                    return true;
                }
                if (!metaTileEntity.getNotifiedFluidInputList().isEmpty()) {
                    canWork = true;
                    invalidatedInputList.clear();
                    metaTileEntity.getNotifiedFluidInputList().clear();
                    metaTileEntity.getNotifiedItemInputList().clear();
                } else {
                    Iterator<IItemHandlerModifiable> iterator = metaTileEntity.getNotifiedItemInputList().iterator();
                    while (iterator.hasNext()) {
                        IItemHandlerModifiable bus = iterator.next();
                        if (invalidatedInputList.remove(bus)) {
                            canWork = true;
                        }
                        iterator.remove();
                    }
                }
                if (!invalidatedInputList.containsAll(getInputBuses())) {
                    canWork = true;
                }
                return canWork;
            }
        }
        return super.canWorkWithInputs();
    }

    @Override
    protected void trySearchNewRecipe() {
        // do not run recipes when there are more than 5 maintenance problems
        // Maintenance can apply to all multiblocks, so cast to a base multiblock class
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (ConfigHolder.machines.enableMaintenance && controller.hasMaintenanceMechanics() && controller.getNumMaintenanceProblems() > 5) {
            return;
        }

        // Distinct buses only apply to some multiblocks, so check the controller against a lower class
        if (controller instanceof RecipeMapMultiblockController) {
            RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;

            if (distinctController.canBeDistinct() && distinctController.isDistinct()) {
                trySearchNewRecipeDistinct();
                return;
            }
        }

        trySearchNewRecipeCombined();
    }

    /**
     * Put into place so multiblocks can override {@link AbstractRecipeLogic#trySearchNewRecipe()} without having to deal with
     * the maintenance and distinct logic in {@link MultiblockRecipeLogic#trySearchNewRecipe()}
     */
    protected void trySearchNewRecipeCombined() {
        super.trySearchNewRecipe();
    }

    protected void trySearchNewRecipeDistinct() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe;
        List<IItemHandlerModifiable> importInventory = getInputBuses();
        IMultipleTankHandler importFluids = getInputTank();

        // Our caching implementation
        // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
        if (checkPreviousRecipeDistinct(importInventory.get(lastRecipeIndex)) && checkRecipe(previousRecipe)) {
            currentRecipe = previousRecipe;
            currentDistinctInputBus = importInventory.get(lastRecipeIndex);
            if (prepareRecipeDistinct(currentRecipe)) {
                // No need to cache the previous recipe here, as it is not null and matched by the current recipe,
                // so it will always be the same
                return;
            }
        }

        // On a cache miss, our efficiency is much worse, as it will check
        // each bus individually instead of the combined inventory all at once.
        for (int i = 0; i < importInventory.size(); i++) {
            IItemHandlerModifiable bus = importInventory.get(i);
            // Skip this bus if no recipe was found last time
            if (invalidatedInputList.contains(bus)) {
                continue;
            }
            // Look for a new recipe after a cache miss
            currentRecipe = findRecipe(maxVoltage, bus, importFluids);
            // Cache the current recipe, if one is found
            if (currentRecipe != null && checkRecipe(currentRecipe)) {
                this.previousRecipe = currentRecipe;
                currentDistinctInputBus = bus;
                if (prepareRecipeDistinct(currentRecipe)) {
                    lastRecipeIndex = i;
                    return;
                }
            }
            if (currentRecipe == null) {
                //no valid recipe found, invalidate this bus
                invalidatedInputList.add(bus);
            }
        }
    }

    @Override
    public void invalidateInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;
        if (distinctController.canBeDistinct() && distinctController.isDistinct()) {
            invalidatedInputList.add(currentDistinctInputBus);
        } else {
            super.invalidateInputs();
        }
    }

    protected boolean checkPreviousRecipeDistinct(IItemHandlerModifiable previousBus) {
        return previousRecipe != null && previousRecipe.matches(false, previousBus, getInputTank());
    }

    protected boolean prepareRecipeDistinct(Recipe recipe) {

        recipe = recipe.trimRecipeOutputs(recipe, getRecipeMap(), metaTileEntity.getItemOutputLimit(), metaTileEntity.getFluidOutputLimit());

        recipe = findParallelRecipe(
                this,
                recipe,
                currentDistinctInputBus,
                getInputTank(),
                getOutputInventory(),
                getOutputTank(),
                getMaxVoltage(),
                getParallelLimit());

        if (recipe != null && setupAndConsumeRecipeInputs(recipe, currentDistinctInputBus)) {
            setupRecipe(recipe);
            return true;
        }

        return false;
    }

    @Override
    protected int[] runOverclockingLogic(IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int maxOverclocks) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        int[] overclock = null;
        if (maintenanceValues.getSecond() != 1.0)

            overclock = standardOverclockingLogic(Math.abs(recipeEUt),
                    maxVoltage,
                    (int) Math.round(recipeDuration * maintenanceValues.getSecond()),
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier(),
                    maxOverclocks
            );

        if (overclock == null)
            overclock = standardOverclockingLogic(Math.abs(recipeEUt),
                    maxVoltage,
                    recipeDuration,
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier(),
                    maxOverclocks);

        if (maintenanceValues.getFirst() > 0)
            overclock[1] = (int) (overclock[1] * (1 + 0.1 * maintenanceValues.getFirst()));

        return overclock;
    }

    @Override
    public long getMaximumOverclockVoltage() {
        return getMaxVoltage();
    }

    protected Tuple<Integer, Double> getMaintenanceValues() {
        MultiblockWithDisplayBase displayBase = this.metaTileEntity instanceof MultiblockWithDisplayBase ? (MultiblockWithDisplayBase) metaTileEntity : null;
        int numMaintenanceProblems = displayBase == null || !displayBase.hasMaintenanceMechanics() || !ConfigHolder.machines.enableMaintenance ? 0 : displayBase.getNumMaintenanceProblems();
        double durationMultiplier = 1.0D;
        if (displayBase != null && displayBase.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
            IMaintenanceHatch hatch = displayBase.getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
            durationMultiplier = hatch.getDurationMultiplier();
        }
        return new Tuple<>(numMaintenanceProblems, durationMultiplier);
    }

    @Override
    protected boolean checkRecipe(@Nonnull Recipe recipe) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {
            controller.checkRecipe(recipe, true);
            return super.checkRecipe(recipe);
        }
        return false;
    }

    @Override
    protected void completeRecipe() {
        performMaintenanceMufflerOperations();
        super.completeRecipe();
    }

    protected void performMaintenanceMufflerOperations() {
        if (metaTileEntity instanceof MultiblockWithDisplayBase) {
            MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;

            // output muffler items
            if (controller.hasMufflerMechanics()) {
                if (parallelRecipesPerformed > 1)
                    controller.outputRecoveryItems(parallelRecipesPerformed);
                else controller.outputRecoveryItems();
            }

            // increase total on time
            if (controller.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance)
                controller.calculateMaintenance(this.progressTime);
        }
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return getEnergyContainer().getInputPerSec();
    }

    @Override
    protected long getEnergyStored() {
        return getEnergyContainer().getEnergyStored();
    }

    @Override
    protected long getEnergyCapacity() {
        return getEnergyContainer().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) getEnergyContainer().changeEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    protected long getMaxVoltage() {
        return Math.max(getEnergyContainer().getInputVoltage(), getEnergyContainer().getOutputVoltage());
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        // if the multiblock has more than one RecipeMap, return the currently selected one
        if (metaTileEntity instanceof IMultipleRecipeMaps)
                return ((IMultipleRecipeMaps) metaTileEntity).getCurrentRecipeMap();
        return super.getRecipeMap();
    }
}
