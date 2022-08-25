package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class FilteredItemHandler extends ItemStackHandler {

    public static Predicate<ItemStack> getCapabilityFilter(Capability<?> cap) {
        return stack -> stack.hasCapability(cap, null);
    }

    private Predicate<ItemStack> fillPredicate;
    private Predicate<ItemStack> extractPredicate;

    public FilteredItemHandler() {
        super(1);
    }

    public FilteredItemHandler(int size) {
        super(size);
    }

    public FilteredItemHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public FilteredItemHandler setFillPredicate(Predicate<ItemStack> fillPredicate) {
        this.fillPredicate = fillPredicate;
        return this;
    }

    public FilteredItemHandler setExtractPredicate(Predicate<ItemStack> extractPredicate) {
        this.extractPredicate = extractPredicate;
        return this;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return fillPredicate == null || fillPredicate.test(stack);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (extractPredicate != null) {
            if (extractPredicate.test(super.extractItem(slot, amount, true))) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }
}
