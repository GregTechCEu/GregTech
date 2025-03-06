package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeNoOverclockingOperator;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeMapPrimitiveMultiblockController extends RecipeMapMultiblockController {

    public RecipeMapPrimitiveMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setOverclockFactory(RecipeNoOverclockingOperator::create)
                .setParallelLimit(null);
    }

    // just initialize inventories based on RecipeMap values by default
    @Override
    protected void initializeAbilities() {
        this.importItems = new NotifiableItemStackHandler(this, getRecipeMap().getMaxInputs(), this,
                false);
        this.importFluids = new FluidTankList(true,
                makeFluidTanks(getRecipeMap().getMaxFluidInputs(), false));
        this.exportItems = new NotifiableItemStackHandler(this, getRecipeMap().getMaxOutputs(), this,
                true);
        this.exportFluids = new FluidTankList(false,
                makeFluidTanks(getRecipeMap().getMaxFluidOutputs(), true));

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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if ((capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) && side != null) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return isStructureFormed();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
