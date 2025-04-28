package gtqt.api.util;

import appeng.items.misc.ItemEncodedPattern;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class PatternItemFilter implements IAEItemFilter {

    public static final PatternItemFilter INSTANCE = new PatternItemFilter();

    @Override
    public boolean allowExtract(final IItemHandler inv, final int slot, final int amount) {
        return true;
    }

    @Override
    public boolean allowInsert(final IItemHandler inv, final int slot, final ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getItem() instanceof ItemEncodedPattern;
    }

}
