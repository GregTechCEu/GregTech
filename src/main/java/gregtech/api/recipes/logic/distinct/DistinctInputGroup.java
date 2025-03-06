package gregtech.api.recipes.logic.distinct;

import gregtech.api.capability.IMultipleTankHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DistinctInputGroup {

    @NotNull
    IItemHandlerModifiable itemInventory();

    @NotNull
    List<ItemStack> itemInventoryView();

    boolean containsItemHandler(IItemHandlerModifiable handler);

    @NotNull
    IMultipleTankHandler fluidInventory();

    @NotNull
    List<FluidStack> fluidInventoryView();

    boolean containsFluidHandler(IFluidHandler handler);

    void setAwaitingUpdate(boolean awaiting);

    boolean isAwaitingUpdate();
}
