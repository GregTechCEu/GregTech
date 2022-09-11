package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import java.util.function.Predicate;

public class FilteredItemHandler extends ItemStackHandler {

    public static Predicate<ItemStack> getCapabilityFilter(Capability<?> cap) {
        return stack -> stack.hasCapability(cap, null);
    }

    private Predicate<ItemStack> fillPredicate;

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

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return fillPredicate == null || fillPredicate.test(stack);
    }
}
