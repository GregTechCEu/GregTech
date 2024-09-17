package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.lookup.property.BiomeInhabitedProperty;
import gregtech.api.recipes.lookup.property.CleanroomFulfilmentProperty;
import gregtech.api.recipes.lookup.property.DimensionInhabitedProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import org.jetbrains.annotations.NotNull;

/**
 * Recipe Logic for a Multiblock that does not require power.
 */
public class PrimitiveRecipeLogic extends DistributedRecipeLogic {

    public PrimitiveRecipeLogic(RecipeMapPrimitiveMultiblockController tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity, recipeMap, false);
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        return true;
    }

    @Override
    protected boolean produceEnergy(long eu, boolean simulate) {
        return true;
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = PropertySet.empty();
        set.add(new DimensionInhabitedProperty(this.getMetaTileEntity().getWorld().provider.getDimension()));
        set.add(new BiomeInhabitedProperty(this.getMetaTileEntity().getWorld().getBiomeForCoordsBody(this.getMetaTileEntity().getPos())));
        set.add(new CleanroomFulfilmentProperty(getCleanroomPredicate()));
        return set;
    }

    @Override
    public long getMaxVoltageIn() {
        return 0;
    }

    @Override
    public long getMaxVoltageOut() {
        return 0;
    }

    @Override
    public long getMaxAmperageIn() {
        return 0;
    }

    @Override
    public long getMaxAmperageOut() {
        return 0;
    }

    @Override
    protected boolean canSubtick() {
        return false;
    }
}
