package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.recipes.RecipeMap;

/**
 * Recipe Logic for a Multiblock that does not require power.
 */
public class PrimitiveRecipeLogic extends AbstractRecipeLogic {

    public PrimitiveRecipeLogic(RecipeMapPrimitiveMultiblockController tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity, recipeMap);
    }

    @Override
    protected long getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt) {
        return true; // spoof energy being drawn
    }

    @Override
    protected long getMaxVoltage() {
        return GTValues.LV;
    }

    @Override
    protected int[] calculateOverclock(int EUt, long voltage, int duration) {
        return new int[]{1, duration};
    }

    @Override
    protected int getOverclockingTier(long voltage) {
        return GTValues.LV; // just return something reasonable
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    public void invalidate() {
        lastItemInputs = null;
        lastFluidInputs = null;
        previousRecipe = null;
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        setActive(false); // this marks dirty for us
    }
}
