package gregtech.common.inventory;

import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.item.ItemStack;

public class SimpleItemInfo implements IItemInfo {

    private final ItemStack itemStack;
    private int totalItemAmount = 0;

    public SimpleItemInfo(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setTotalItemAmount(int totalItemAmount) {
        this.totalItemAmount = totalItemAmount;
    }

    @Override
    public int getTotalItemAmount() {
        return totalItemAmount;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleItemInfo)) return false;
        SimpleItemInfo that = (SimpleItemInfo) o;
        return itemStack.equals(that.itemStack);
    }

    @Override
    public int hashCode() {
        return ItemStackHashStrategy.comparingAllButCount().hashCode(this.itemStack);
    }
}
