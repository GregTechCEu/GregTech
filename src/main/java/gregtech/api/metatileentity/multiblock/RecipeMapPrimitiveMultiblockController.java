package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.impl.*;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeMapPrimitiveMultiblockController extends MultiblockWithDisplayBase {

    protected PrimitiveRecipeLogic recipeMapWorkable;

    public RecipeMapPrimitiveMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId);
        this.recipeMapWorkable = new PrimitiveRecipeLogic(this, recipeMap);
        initializeAbilities();
    }

    // just initialize inventories based on RecipeMap values by default
    protected void initializeAbilities() {
        this.importItems = new ItemStackHandler(recipeMapWorkable.recipeMap.getMaxInputs());
        this.importFluids = new FluidTankList(true, makeFluidTanks(recipeMapWorkable.recipeMap.getMaxFluidInputs()));
        this.exportItems = new ItemStackHandler(recipeMapWorkable.recipeMap.getMaxOutputs());
        this.exportFluids = new FluidTankList(false, makeFluidTanks(recipeMapWorkable.recipeMap.getMaxFluidOutputs()));

        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }

    private List<FluidTank> makeFluidTanks(int length) {
        List<FluidTank> fluidTankList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            fluidTankList.add(new FluidTank(32000));
        }
        return fluidTankList;
    }

    @Override
    protected void updateFormedValid() {
        recipeMapWorkable.update();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        recipeMapWorkable.invalidate();
    }
}
