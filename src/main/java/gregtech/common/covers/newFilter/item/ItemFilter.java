package gregtech.common.covers.newFilter.item;

import gregtech.common.covers.newFilter.Filter;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

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

    @Override
    public int getTransferLimit(Object stack, int globalTransferLimit) {
        return globalTransferLimit;
    }

    @Nullable
    public abstract Object matchItemStack(ItemStack itemStack);

    @Override
    public boolean matches(ItemStack stack, boolean ignoreInverted) {
        return matchItemStack(stack) == null == (!ignoreInverted && isInverted());
    }
}
