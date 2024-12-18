package gregtech.api.capability.impl;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class NotifiableFilteredItemHandler extends NotifiableItemStackHandler {

    private @Nullable Predicate<ItemStack> predicate;

    public NotifiableFilteredItemHandler(@NotNull MetaTileEntity metaTileEntity, int slots,
                                         @NotNull MetaTileEntity entityToNotify, boolean isExport) {
        super(metaTileEntity, slots, entityToNotify, isExport);
    }

    public @NotNull NotifiableFilteredItemHandler setFillPredicate(@Nullable Predicate<ItemStack> predicate) {
        this.predicate = predicate;
        return this;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return predicate == null || predicate.test(stack);
    }
}
