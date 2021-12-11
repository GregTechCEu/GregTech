package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;

public abstract class FuelMultiblockController extends RecipeMapMultiblockController {

    public FuelMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
        this.recipeMapWorkable.enableOverclockVoltage();
        this.recipeMapWorkable.setOverclockTier((int) tier);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }
}
