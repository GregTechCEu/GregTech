package gregtech.api.capability.impl;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

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

    public FilteredItemHandler(MetaTileEntity metaTileEntity, NonNullList<ItemStack> stacks) {
        super(metaTileEntity, stacks);
    }

    public FilteredItemHandler setFillPredicate(Predicate<ItemStack> fillPredicate) {
        this.fillPredicate = fillPredicate;
        return this;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return fillPredicate == null || fillPredicate.test(stack);
    }
}
