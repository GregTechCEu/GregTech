package gregtech.api.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class WorkableTieredMetaTileEntity extends TieredMetaTileEntity {

    protected final RecipeLogicEnergy workable;
    protected final OrientedOverlayRenderer renderer;

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer, int tier) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        this.workable = createWorkable(recipeMap);
        initializeInventory();
        reinitializeEnergyContainer();
    }

    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        return new RecipeLogicEnergy(this, recipeMap, () -> energyContainer);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {
            @Override
            public long getInputAmperage() {
                if(getEnergyCapacity() / 2 > getEnergyStored() && workable.isActive()) {
                    return 2;
                }
                return 1;
            }
        };
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        renderer.render(renderState, translation, pipeline, getFrontFacing(), workable.isActive());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (workable == null) return new ItemStackHandler(0);
        return new ItemStackHandler(workable.recipeMap.getMaxInputs());
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (workable == null) return new ItemStackHandler(0);
        return new ItemStackHandler(workable.recipeMap.getMaxOutputs());
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FilteredFluidHandler[] fluidImports = new FilteredFluidHandler[workable.recipeMap.getMaxFluidInputs()];
        for (int i = 0; i < fluidImports.length; i++) {
            FilteredFluidHandler filteredFluidHandler = new FilteredFluidHandler(getInputTankCapacity(i));
            filteredFluidHandler.setFillPredicate(this::canInputFluid);
            fluidImports[i] = filteredFluidHandler;
        }
        return new FluidTankList(false, fluidImports);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FluidTank[] fluidExports = new FluidTank[workable.recipeMap.getMaxFluidOutputs()];
        for (int i = 0; i < fluidExports.length; i++) {
            fluidExports[i] = new FluidTank(getOutputTankCapacity(i));
        }
        return new FluidTankList(false, fluidExports);
    }

    protected boolean canInputFluid(FluidStack inputFluid) {
        RecipeMap<?> recipeMap = workable.recipeMap;
        if (recipeMap.canInputFluidForce(inputFluid.getFluid()))
            return true; //if recipe map forces input of given fluid, return true
        Set<Recipe> matchingRecipes = null;
        for (IFluidTank fluidTank : importFluids) {
            FluidStack fluidInTank = fluidTank.getFluid();
            if (fluidInTank != null) {
                if (matchingRecipes == null) {
                    //if we didn't have a list of recipes with any fluids, obtain it from first tank with fluid
                    matchingRecipes = new HashSet<>(recipeMap.getRecipesForFluid(fluidInTank));
                } else {
                    //else, remove recipes that don't contain fluid in this tank from list
                    matchingRecipes.removeIf(recipe -> !recipe.hasInputFluid(fluidInTank));
                }
            }
        }
        if (matchingRecipes == null) {
            //if all tanks are empty, generally fluid can be inserted if there are recipes for it
            return !recipeMap.getRecipesForFluid(inputFluid).isEmpty();
        } else {
            //otherwise, we can insert fluid only if one of recipes accept it as input
            return matchingRecipes.stream().anyMatch(recipe -> recipe.hasInputFluid(inputFluid));
        }
    }

    protected int getInputTankCapacity(int index) {
        return 64000;
    }

    protected int getOutputTankCapacity(int index) {
        return 64000;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VN[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }
}
