package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.RecipeRunner;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abstract recipe logic that supports distinct input groups for recipe search & run.
 */
public abstract class DistinctRecipeLogic extends AbstractRecipeLogic {

    private static final IMultipleTankHandler EMPTY = new FluidTankList(false);

    protected final List<DistinctInputGroup> defaultInputGroups = Collections.singletonList(new FullInputGroup());

    protected @Nullable DistinctInputGroup currentInputGroup;

    public DistinctRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity, recipeMap);
    }

    /**
     * @deprecated input inventory must be accessed through input groups instead.
     * @return an empty item handler
     */
    @Override
    @Deprecated
    protected final IItemHandlerModifiable getInputInventory() {
        return (IItemHandlerModifiable) EmptyHandler.INSTANCE;
    }

    /**
     * @deprecated input tank must be accessed through input groups instead.
     * @return an empty tank
     */
    @Override
    @Deprecated
    protected final IMultipleTankHandler getInputTank() {
        return EMPTY;
    }

    public Collection<DistinctInputGroup> getInputGroups() {
        return defaultInputGroups;
    }

    @Override
    protected void findAndSetupRecipeToRun(@NotNull List<ItemStack> listViewOfItemInputs,
                                           @NotNull List<FluidStack> listViewOfFluidInputs,
                                           @NotNull PropertySet properties, @NotNull RecipeRunner runner) {
        updateGroupValidity();
        Iterator<DistinctInputGroup> iterator = getInputGroups().iterator();
        while (runner.getCurrent() == null && iterator.hasNext()) {
            DistinctInputGroup group = iterator.next();
            currentInputGroup = group;
            super.findAndSetupRecipeToRun(group.itemInventoryView(), group.fluidInventoryView(), properties, runner);
        }
        currentInputGroup = null;
    }

    @Override
    public void setInvalidItemInputs(boolean invalidItemInputs) {
        if (currentInputGroup == null)
            super.setInvalidItemInputs(invalidItemInputs);
        else currentInputGroup.setInvalidItemInputs(invalidItemInputs);
    }

    @Override
    public void setInvalidFluidInputs(boolean invalidFluidInputs) {
        if (currentInputGroup == null)
            super.setInvalidFluidInputs(invalidFluidInputs);
        else currentInputGroup.setInvalidFluidInputs(invalidFluidInputs);
    }

    @Override
    protected boolean canWorkWithInputs() {
        if (currentInputGroup == null)
            return super.canWorkWithInputs();
        else {
            return !currentInputGroup.areFluidInputsInvalid() && !currentInputGroup.areItemInputsInvalid();
        }
    }

    protected void updateGroupValidity() {
        Collection<DistinctInputGroup> inputGroups = getInputGroups();
        List<IItemHandlerModifiable> notifiedItems = metaTileEntity.getNotifiedItemInputList();
        for (IItemHandlerModifiable handler : notifiedItems) {
            for (DistinctInputGroup group : inputGroups) {
                if (group.containsItemHandler(handler)) {
                    setOutputInvalid(false);
                    group.setInvalidItemInputs(false);
                }
            }
        }
        notifiedItems.clear();
        List<IFluidHandler> notifiedFluids = metaTileEntity.getNotifiedFluidInputList();
        for (IFluidHandler handler : notifiedFluids) {
            for (DistinctInputGroup group : inputGroups) {
                if (group.containsFluidHandler(handler)) {
                    setOutputInvalid(false);
                    group.setInvalidFluidInputs(false);
                }
            }
        }
        notifiedFluids.clear();
    }

    public interface DistinctInputGroup {

        @NotNull
        List<ItemStack> itemInventoryView();

        boolean containsItemHandler(IItemHandlerModifiable handler);

        @NotNull
        List<FluidStack> fluidInventoryView();

        boolean containsFluidHandler(IFluidHandler handler);

        void setInvalidItemInputs(boolean invalidInputs);

        void setInvalidFluidInputs(boolean invalidInputs);

        boolean areItemInputsInvalid();

        boolean areFluidInputsInvalid();

    }

    public static final class DefaultInputGroup implements DistinctInputGroup {

        private final @NotNull List<ItemStack> inventoryView;
        private final @NotNull List<FluidStack> fluidView;

        private final @NotNull Set<IItemHandlerModifiable> inputBuses;
        private final @NotNull Set<IFluidHandler> inputHatches;

        private boolean invalidFluid = false;
        private boolean invalidItem = false;

        public DefaultInputGroup(@NotNull IItemHandlerModifiable inventoryView,
                                 @NotNull Set<IItemHandlerModifiable> inputBuses,
                                 @NotNull IMultipleTankHandler fluidView,
                                 @NotNull Set<IFluidHandler> inputHatches) {
            this.inventoryView = GTUtility.itemHandlerToList(inventoryView);
            this.fluidView = GTUtility.fluidHandlerToList(fluidView);
            this.inputBuses = inputBuses;
            this.inputHatches = inputHatches;
        }

        @Override
        public @NotNull List<ItemStack> itemInventoryView() {
            return inventoryView;
        }

        @Override
        public boolean containsItemHandler(IItemHandlerModifiable handler) {
            return inputBuses.contains(handler);
        }

        @Override
        public @NotNull List<FluidStack> fluidInventoryView() {
            return fluidView;
        }

        @Override
        public boolean containsFluidHandler(IFluidHandler handler) {
            return inputHatches.contains(handler);
        }

        @Override
        public void setInvalidItemInputs(boolean invalidInputs) {
            invalidItem = invalidInputs;
        }

        @Override
        public void setInvalidFluidInputs(boolean invalidInputs) {
            invalidFluid = invalidInputs;
        }

        @Override
        public boolean areItemInputsInvalid() {
            return invalidItem;
        }

        @Override
        public boolean areFluidInputsInvalid() {
            return invalidFluid;
        }

    }

    private final class FullInputGroup implements DistinctInputGroup {

        @Override
        public @NotNull List<ItemStack> itemInventoryView() {
            return GTUtility.itemHandlerToList(DistinctRecipeLogic.super.getInputInventory());
        }

        @Override
        public boolean containsItemHandler(IItemHandlerModifiable handler) {
            return true;
        }

        @Override
        public @NotNull List<FluidStack> fluidInventoryView() {
            return GTUtility.fluidHandlerToList(DistinctRecipeLogic.super.getInputTank());
        }

        @Override
        public boolean containsFluidHandler(IFluidHandler handler) {
            return true;
        }

        @Override
        public void setInvalidItemInputs(boolean invalidInputs) {
            DistinctRecipeLogic.super.setInvalidItemInputs(invalidInputs);
        }

        @Override
        public void setInvalidFluidInputs(boolean invalidInputs) {
            DistinctRecipeLogic.super.setInvalidFluidInputs(invalidInputs);
        }

        @Override
        public boolean areItemInputsInvalid() {
            return hasInvalidItemInputs();
        }

        @Override
        public boolean areFluidInputsInvalid() {
            return hasInvalidFluidInputs();
        }

    }
}
