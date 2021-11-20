package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

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
    protected void trySearchNewRecipe() {
        // do not run recipes when there are more than 5 maintenance problems
        // Maintenance can apply to all multiblocks, so cast to a base multiblock class
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (controller.hasMaintenanceMechanics() && controller.getNumMaintenanceProblems() > 5) {
            return;
        }

        // Distinct buses only apply to some of the multiblocks, so check the controller against a lower class
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
        IItemHandlerModifiable exportInventory = getOutputInventory();
        IMultipleTankHandler exportFluids = getOutputTank();

        //if fluids changed, iterate all input busses again
        if (metaTileEntity.getNotifiedFluidInputList().size() > 0) {
            for (IItemHandlerModifiable ihm : importInventory) {
                if (!metaTileEntity.getNotifiedItemInputList().contains(ihm)) {
                    metaTileEntity.getNotifiedItemInputList().add(ihm);
                }
            }
            metaTileEntity.getNotifiedFluidInputList().clear();
        }

        // Our caching implementation
        // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
        if (previousRecipe != null && previousRecipe.matches(false, importInventory.get(lastRecipeIndex), importFluids)) {
            currentRecipe = previousRecipe;
            currentDistinctInputBus = importInventory.get(lastRecipeIndex);
            currentRecipe = findParallelRecipe(
                    this,
                    currentRecipe,
                    importInventory.get(lastRecipeIndex),
                    importFluids,
                    exportInventory,
                    exportFluids,
                    maxVoltage, metaTileEntity.getParallelLimit());

            // If a valid recipe is found, immediately attempt to return it to prevent inventory scanning
            if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe, importInventory.get(lastRecipeIndex))) {
                setupRecipe(currentRecipe);
                metaTileEntity.getNotifiedItemInputList().remove(importInventory.get(lastRecipeIndex));

                // No need to cache the previous recipe here, as it is not null and matched by the current recipe,
                // so it will always be the same
                return;
            }
        }

        // On a cache miss, our efficiency is much worse, as it will check
        // each bus individually instead of the combined inventory all at once.
        for (int i = 0; i < importInventory.size(); i++) {
            IItemHandlerModifiable bus = importInventory.get(i);
            // Skip this bus if no recipe was found last time and the inventory did not change
            if (invalidatedInputList.contains(bus) && !metaTileEntity.getNotifiedItemInputList().contains(bus)) {
                continue;
            } else {
                invalidatedInputList.remove(bus);
            }
            // Look for a new recipe after a cache miss
            currentRecipe = findRecipe(maxVoltage, bus, importFluids, MatchingMode.DEFAULT);
            // Cache the current recipe, if one is found
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
                currentDistinctInputBus = bus;
                currentRecipe = findParallelRecipe(
                        this,
                        currentRecipe,
                        importInventory.get(i),
                        importFluids,
                        exportInventory,
                        exportFluids,
                        maxVoltage, metaTileEntity.getParallelLimit());

                if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe, importInventory.get(i))) {
                    lastRecipeIndex = i;
                    setupRecipe(currentRecipe);
                    metaTileEntity.getNotifiedItemInputList().remove(bus);
                    return;
                }
            } else {
                invalidatedInputList.add(bus);
            }
        }

        //If no matching recipes are found, clear the notified inputs so we know when new items are given
        metaTileEntity.getNotifiedItemInputList().clear();
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

    @Override
    protected int[] calculateOverclock(int EUt, long voltage, int duration) {
        // apply maintenance penalties
        MultiblockWithDisplayBase displayBase = this.metaTileEntity instanceof MultiblockWithDisplayBase ? (MultiblockWithDisplayBase) metaTileEntity : null;
        int numMaintenanceProblems = displayBase == null ? 0 : displayBase.getNumMaintenanceProblems();

        int[] overclock = null;
        if (displayBase != null && displayBase.hasMaintenanceMechanics()) {
            IMaintenanceHatch hatch = displayBase.getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
            double durationMultiplier = hatch.getDurationMultiplier();
            if (durationMultiplier != 1.0) {
                overclock = super.calculateOverclock(EUt, voltage, (int) Math.round(duration * durationMultiplier));
            }
        }
        if (overclock == null) overclock = super.calculateOverclock(EUt, voltage, duration);
        overclock[1] = (int) (overclock[1] * (1 + 0.1 * numMaintenanceProblems));

        return overclock;
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false) &&
                super.setupAndConsumeRecipeInputs(recipe, importInventory)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
    }

    @Override
    protected void completeRecipe() {
        if (metaTileEntity instanceof MultiblockWithDisplayBase) {
            MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;

            // output muffler items
            if (controller.hasMufflerMechanics()) {
                if (parallelRecipesPerformed > 1)
                    controller.outputRecoveryItems(parallelRecipesPerformed);
                else controller.outputRecoveryItems();
            }

            // increase total on time
            if (controller.hasMaintenanceMechanics())
                controller.calculateMaintenance(this.progressTime);
        }
        super.completeRecipe();
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
    protected boolean drawEnergy(int recipeEUt) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            getEnergyContainer().changeEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    protected long getMaxVoltage() {
        return Math.max(getEnergyContainer().getInputVoltage(), getEnergyContainer().getOutputVoltage());
    }
}
