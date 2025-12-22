package gregtech.common.covers.filter;

import gregtech.common.covers.filter.readers.SimpleItemFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SimpleItemFilter extends BaseFilter {

    private static final int MAX_MATCH_SLOTS = 9;
    private final SimpleItemFilterReader filterReader = new SimpleItemFilterReader(MAX_MATCH_SLOTS);

    @Override
    public SimpleItemFilterReader getFilterReader() {
        return filterReader;
    }

    @Override
    public MatchResult matchItem(ItemStack itemStack) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                itemStack);
        return MatchResult.create(matchedSlot != -1 == !isBlacklistFilter(),
                filterReader.getStackInSlot(matchedSlot), matchedSlot);
    }

    @Override
    public boolean testItem(ItemStack toTest) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                toTest);
        return matchedSlot != -1;
    }

    @Override
    public int getTransferLimit(int matchSlot, int transferSize) {
        ItemStack stackInFilterSlot = filterReader.getStackInSlot(matchSlot);
        return Math.min(stackInFilterSlot.getCount(), transferSize);
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM;
    }

    @Override
    public int getTransferLimit(ItemStack stack, int transferSize) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                stack);
        return getTransferLimit(matchedSlot, transferSize);
    }

    @Override
    public BaseFilter copy() {
        return new SimpleItemFilter();
    }

    public static int itemFilterMatch(IItemHandler filterSlots, boolean ignoreDamage,
                                      boolean ignoreNBTData, ItemStack itemStack) {
        for (int i = 0; i < filterSlots.getSlots(); i++) {
            ItemStack filterStack = filterSlots.getStackInSlot(i);
            if (!filterStack.isEmpty() && areItemsEqual(ignoreDamage, ignoreNBTData, filterStack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean areItemsEqual(boolean ignoreDamage, boolean ignoreNBTData,
                                         ItemStack filterStack, ItemStack itemStack) {
        if (ignoreDamage) {
            if (!filterStack.isItemEqualIgnoreDurability(itemStack)) {
                return false;
            }
        } else if (!filterStack.isItemEqual(itemStack)) {
            return false;
        }
        return ignoreNBTData || ItemStack.areItemStackTagsEqual(filterStack, itemStack);
    }
}
