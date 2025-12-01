package gregtech.api.capability.impl;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FilteredItemHandler extends GTItemStackHandler {

    public static Predicate<ItemStack> getCapabilityFilter(Capability<?> cap) {
        return stack -> stack.hasCapability(cap, null);
    }

    private Predicate<ItemStack> fillPredicate;

    public FilteredItemHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity, 1);
    }

    public FilteredItemHandler(MetaTileEntity metaTileEntity, int size) {
        super(metaTileEntity, size);
    }

    public FilteredItemHandler(MetaTileEntity metaTileEntity, int size, Predicate<ItemStack> fillPredicate) {
        super(metaTileEntity, size);
        this.fillPredicate = fillPredicate;
    }

    public FilteredItemHandler(MetaTileEntity metaTileEntity, NonNullList<ItemStack> stacks) {
        super(metaTileEntity, stacks);
    }

    public FilteredItemHandler setFillPredicate(Predicate<ItemStack> fillPredicate) {
        this.fillPredicate = fillPredicate;
        return this;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (!isItemValid(slot, stack)) return;
        super.setStackInSlot(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return fillPredicate == null || fillPredicate.test(stack);
    }
}
