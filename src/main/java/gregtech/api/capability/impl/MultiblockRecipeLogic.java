package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
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
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false) &&
            super.setupAndConsumeRecipeInputs(recipe)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
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
