package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.impl.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.sound.ISoundCreator;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeMapPrimitiveMultiblockController extends MultiblockWithDisplayBase implements ISoundCreator {

    protected PrimitiveRecipeLogic recipeMapWorkable;

    public RecipeMapPrimitiveMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId);
        this.recipeMapWorkable = new PrimitiveRecipeLogic(this, recipeMap);
        initializeAbilities();
    }

    // just initialize inventories based on RecipeMap values by default
    protected void initializeAbilities() {
        this.importItems = new NotifiableItemStackHandler(recipeMapWorkable.getRecipeMap().getMaxInputs(), this, false);
        this.importFluids = new FluidTankList(true, makeFluidTanks(recipeMapWorkable.getRecipeMap().getMaxFluidInputs(), false));
        this.exportItems = new NotifiableItemStackHandler(recipeMapWorkable.getRecipeMap().getMaxOutputs(), this, true);
        this.exportFluids = new FluidTankList(false, makeFluidTanks(recipeMapWorkable.getRecipeMap().getMaxFluidOutputs(), true));

        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }

    private List<FluidTank> makeFluidTanks(int length, boolean isExport) {
        List<FluidTank> fluidTankList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            fluidTankList.add(new NotifiableFluidTank(32000, this, isExport));
        }
        return fluidTankList;
    }

    @Override
    protected void updateFormedValid() {
        recipeMapWorkable.update();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.recipeMapWorkable.isWorkingEnabled() && this.recipeMapWorkable.isActive();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        recipeMapWorkable.invalidate();
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof PrimitiveRecipeLogic);
    }

    @Override
    public void onAttached(Object... data) {
        super.onAttached(data);
        if (getWorld() != null && getWorld().isRemote) {
            this.setupSound(recipeMapWorkable.getRecipeMap().getSound(), this.getPos());
        }
    }

    public boolean canCreateSound() {
        return recipeMapWorkable.isActive();
    }

}
