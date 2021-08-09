package gregtech.common.metatileentities.electric;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.GasCollectorDimensionProperty;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityGasCollector extends SimpleMachineMetaTileEntity {

    private int currentDimension;

    public MetaTileEntityGasCollector(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer, int tier, boolean hasFrontFacing) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityGasCollector(this.metaTileEntityId, RecipeMaps.GAS_COLLECTOR_RECIPES, Textures.GAS_COLLECTOR_OVERLAY, this.getTier(), hasFrontFacing());
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % 20 == 0)
            this.currentDimension = this.getWorld().provider.getDimension();
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        final RecipeLogicEnergy result = new RecipeLogicEnergy(this, recipeMap, () -> energyContainer) {
            @Override
            protected void trySearchNewRecipe() {
                long maxVoltage = getMaxVoltage();
                Recipe currentRecipe = null;
                IItemHandlerModifiable importInventory = getInputInventory();
                IMultipleTankHandler importFluids = getInputTank();
                if (previousRecipe != null && previousRecipe.matches(false, importInventory, importFluids)) {
                    //if previous recipe still matches inputs, try to use it
                    currentRecipe = previousRecipe;
                } else {
                    boolean dirty = checkRecipeInputsDirty(importInventory, importFluids);
                    if (dirty || forceRecipeRecheck) {
                        this.forceRecipeRecheck = false;
                        //else, try searching new recipe for given inputs
                        currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.DEFAULT);
                        if (currentRecipe != null) {
                            List<Integer> recipeDimensions = currentRecipe.getProperty(GasCollectorDimensionProperty.getInstance(), new ArrayList<>());
                            boolean isDimensionValid = false;
                            for (Integer dimension : recipeDimensions) {
                                if (dimension == currentDimension) {
                                    this.previousRecipe = currentRecipe;
                                    isDimensionValid = true;
                                    break;
                                }
                            }
                            if (!isDimensionValid)
                                currentRecipe = null;
                        }
                    }
                }
                if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe)) {
                    setupRecipe(currentRecipe);
                }
            }
        };
        result.enableOverclockVoltage();
        return result;
    }
}
