package gregtech.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class GTFluidCraftingIngredient extends Ingredient {
    private final FluidStack fluidStack;

    GTFluidCraftingIngredient(ItemStack... stacks) {
        super(stacks);
        this.fluidStack = stacks[0].getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).drain(Integer.MAX_VALUE, false);
    }

    @Override
    public boolean apply(@Nullable ItemStack testedStack) {
        IFluidHandlerItem handler = null;
        if (testedStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            handler = testedStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        }
        if (handler != null) {
            if (fluidStack == null || fluidStack.amount == 0) return true;
            FluidStack drained = handler.drain(fluidStack, false);
            if (drained != null && drained.amount >= fluidStack.amount) {
                return drained.isFluidEqual(fluidStack);
            }
        }
        return false;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
