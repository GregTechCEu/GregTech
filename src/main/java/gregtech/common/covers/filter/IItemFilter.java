package gregtech.common.covers.filter;

import net.minecraft.item.ItemStack;

public interface IItemFilter extends IFilter {

    MatchResult<ItemStack> match(ItemStack toMatch);

    boolean test(ItemStack toTest);

    default int getTransferLimit(ItemStack stack, int transferSize) {
        return 0;
    }

    default MatchResult<ItemStack> createResult(boolean matched, ItemStack fluidStack, int index) {
        return MatchResult.create(matched, fluidStack, index);
    }
}
