package gregtech.common.covers.newFilter.item;

import gregtech.api.util.ItemStackKey;
import gregtech.common.covers.newFilter.Filter;
import net.minecraft.item.ItemStack;

import java.util.Set;

public abstract class ItemFilter extends Filter<ItemStack> {

    private int maxStackSize = Integer.MAX_VALUE;

    public final int getMaxStackSize() {
        return maxStackSize;
    }

    public final void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        onMaxStackSizeChange();
    }

    protected void onMaxStackSizeChange() {
    }

    public abstract boolean showGlobalTransferLimitSlider();

    public abstract int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit);

    public abstract Object matchItemStack(ItemStack itemStack);

    @Override
    public boolean matches(ItemStack stack) {
        return matchItemStack(stack) == null == isInverted();
    }
}
