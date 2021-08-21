package gregtech.common.metatileentities.electric;

import gregtech.api.capability.IEnergyContainer;
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
import java.util.function.Supplier;

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
        final RecipeLogicEnergy result = new GasCollectorRecipeLogic(this, RecipeMaps.GAS_COLLECTOR_RECIPES, () -> energyContainer);
        result.enableOverclockVoltage();
        return result;
    }

    protected int getCurrentDimension() {
        return this.currentDimension;
    }

    private class GasCollectorRecipeLogic extends RecipeLogicEnergy {

        public GasCollectorRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected void trySearchNewRecipe() {
            long maxVoltage = getMaxVoltage();
            Recipe currentRecipe = null;
            IItemHandlerModifiable importInventory = getInputInventory();
            IMultipleTankHandler importFluids = getInputTank();

            // see if the last recipe we used still works
            if (this.previousRecipe != null && this.previousRecipe.matches(false, importInventory, importFluids, MatchingMode.IGNORE_FLUIDS))
                currentRecipe = this.previousRecipe;
                // If there is no active recipe, then we need to find one.
            else {
                currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.IGNORE_FLUIDS);
                if (currentRecipe != null) {
                    List<Integer> recipeDimensions = currentRecipe.getProperty(GasCollectorDimensionProperty.getInstance(), new ArrayList<>());
                    boolean isDimensionValid = false;
                    for (Integer dimension : recipeDimensions) {
                        if (dimension == getCurrentDimension()) {
                            this.previousRecipe = currentRecipe;
                            isDimensionValid = true;
                            break;
                        }
                    }
                    if (!isDimensionValid)
                        currentRecipe = null;
                }
            }
            this.invalidInputsForRecipes = (currentRecipe == null);

            // proceed if we have a usable recipe.
            if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe))
                setupRecipe(currentRecipe);
            // Inputs have been inspected.
            metaTileEntity.getNotifiedItemInputList().clear();
            metaTileEntity.getNotifiedFluidInputList().clear();
        }
    }
}
