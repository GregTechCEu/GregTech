package gregtech.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

public class GTFluidCraftingIngredient extends Ingredient {

    private final FluidStack fluidStack;

    GTFluidCraftingIngredient(ItemStack... stacks) {
        super(stacks);
        IFluidHandlerItem handler = stacks[0].getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            this.fluidStack = handler.drain(Integer.MAX_VALUE, false);
        } else {
            throw new IllegalArgumentException("The ItemStack " + stacks[0] + " has no FLUID_HANDLER_ITEM_CAPABILITY!");
        }
    }

    @Override
    public boolean apply(@Nullable ItemStack testedStack) {
        if (testedStack == null || testedStack.isEmpty()) return false;
        IFluidHandlerItem handler = null;
        if (!testedStack.getItem().hasContainerItem(testedStack)) {
            return false;
        }
        if (testedStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (testedStack.getCount() > 1) {
                testedStack = testedStack.copy();
                testedStack.setCount(1);
            }
            handler = testedStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        }
        if (handler != null) {
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
