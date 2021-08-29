package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import net.minecraftforge.items.IItemHandlerModifiable;

public class MultiblockRecipeLogic extends AbstractRecipeLogic {


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
        setActive(false); // this marks dirty for us
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
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (controller.hasMaintenanceMechanics() && controller.getNumMaintenanceProblems() > 5)
            return;

        super.trySearchNewRecipe();
    }

    @Override
    protected int[] calculateOverclock(int EUt, long voltage, int duration) {
        // apply maintenance penalties
        int numMaintenanceProblems = (this.metaTileEntity instanceof MultiblockWithDisplayBase) ?
                ((MultiblockWithDisplayBase) metaTileEntity).getNumMaintenanceProblems() : 0;

        int[] overclock = super.calculateOverclock(EUt, voltage, duration);
        overclock[1] = (int) (duration * (1 + 0.1 * numMaintenanceProblems));

        return overclock;
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false) &&
                super.setupAndConsumeRecipeInputs(recipe)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
    }

    @Override
    protected void completeRecipe() {
        if (metaTileEntity instanceof MultiblockWithDisplayBase) {
            MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;

            // output muffler items
            if (controller.hasMufflerMechanics())
                controller.outputRecoveryItems();

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
