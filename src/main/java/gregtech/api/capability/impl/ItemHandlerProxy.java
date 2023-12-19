package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

public class ItemHandlerProxy implements IItemHandler {

    private final IItemHandler insertHandler;
    private final IItemHandler extractHandler;

    public ItemHandlerProxy(IItemHandler insertHandler, IItemHandler extractHandler) {
        this.insertHandler = insertHandler;
        this.extractHandler = extractHandler;
    }

    @Override
    public int getSlots() {
        return insertHandler.getSlots() + extractHandler.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot < insertHandler.getSlots() ? insertHandler.getStackInSlot(slot) :
                extractHandler.getStackInSlot(slot - insertHandler.getSlots());
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return slot < insertHandler.getSlots() ? insertHandler.insertItem(slot, stack, simulate) : stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot >= insertHandler.getSlots() ?
                extractHandler.extractItem(slot - insertHandler.getSlots(), amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot < insertHandler.getSlots() ? insertHandler.getSlotLimit(slot) :
                extractHandler.getSlotLimit(slot - insertHandler.getSlots());
    }
}
