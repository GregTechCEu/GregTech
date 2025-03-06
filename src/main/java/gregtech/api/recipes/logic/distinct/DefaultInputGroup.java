package gregtech.api.recipes.logic.distinct;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class DefaultInputGroup implements DistinctInputGroup {

    private final @NotNull IItemHandlerModifiable inventory;
    private final @NotNull List<ItemStack> inventoryView;
    private final @NotNull IMultipleTankHandler fluids;
    private final @NotNull List<FluidStack> fluidView;

    private final @NotNull Set<IItemHandlerModifiable> inputBuses;
    private final @NotNull Set<IFluidHandler> inputHatches;

    private boolean invalid = false;

    public DefaultInputGroup(@NotNull IItemHandlerModifiable inventoryView,
                             @NotNull Set<IItemHandlerModifiable> inputBuses,
                             @NotNull IMultipleTankHandler fluidView,
                             @NotNull Set<IFluidHandler> inputHatches) {
        this.inventory = inventoryView;
        this.inventoryView = GTUtility.itemHandlerToList(inventoryView);
        this.fluids = fluidView;
        this.fluidView = GTUtility.fluidHandlerToList(fluidView);
        this.inputBuses = inputBuses;
        this.inputHatches = inputHatches;
    }

    @Override
    public @NotNull IItemHandlerModifiable itemInventory() {
        return inventory;
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
    public @NotNull IMultipleTankHandler fluidInventory() {
        return fluids;
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
    public void setAwaitingUpdate(boolean awaiting) {
        this.invalid = awaiting;
    }

    @Override
    public boolean isAwaitingUpdate() {
        return invalid;
    }
}
