package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * Simple item handler to allow for fluid capable items to be inserted / extracted from blocks such as tanks.
 * See {@link gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch} for usage example.
 */
public class SimpleFluidItemHandler implements IItemHandler {
    private final ItemStackHandler inventory;

    private final int inputSlot;
    private final int outputSlot;

    public SimpleFluidItemHandler(ItemStackHandler inventory, int inputSlot, int outputSlot) {
        this.inventory = inventory;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public int getSlotLimit(int i) {
        return inventory.getSlotLimit(inputSlot);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return inventory.getStackInSlot(outputSlot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Verify that item has a fluid handler and can fit
        IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(stack);
        if (fluidHandlerItem == null
                || !inventory.isItemValid(inputSlot, stack)) {
            return stack;
        }

        return inventory.insertItem(0, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = inventory.getStackInSlot(outputSlot);
        if (stack.isEmpty() || amount == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack extractedStack = stack.copy();
        extractedStack.setCount(Math.min(amount, stack.getCount()));
        if (!simulate) {
            if (stack.getCount() <= amount) {
                inventory.setStackInSlot(outputSlot, ItemStack.EMPTY);
            } else {
                stack.setCount(stack.getCount() - amount);
            }
        }

        return extractedStack;
    }
}
