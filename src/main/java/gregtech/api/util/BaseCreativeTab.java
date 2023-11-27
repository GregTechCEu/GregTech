package gregtech.api.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BaseCreativeTab extends CreativeTabs {

    private final boolean hasSearchBar;
    private final Supplier<ItemStack> iconSupplier;

    public BaseCreativeTab(String TabName, Supplier<ItemStack> iconSupplier, boolean hasSearchBar) {
        super(TabName);
        this.iconSupplier = iconSupplier;
        this.hasSearchBar = hasSearchBar;

        if (hasSearchBar)
            setBackgroundImageName("item_search.png");
    }

    @NotNull
    @Override
    public ItemStack createIcon() {
        if (iconSupplier == null) {
            GTLog.logger.error("Icon supplier was null for CreativeTab " + getTabLabel());
            return new ItemStack(Blocks.STONE);
        }

        ItemStack stack = iconSupplier.get();
        if (stack == null) {
            GTLog.logger.error("Icon supplier return null for CreativeTab " + getTabLabel());
            return new ItemStack(Blocks.STONE);
        }

        if (stack == ItemStack.EMPTY) {
            GTLog.logger.error("Icon built from iconSupplied is EMPTY for CreativeTab " + getTabLabel());
            return new ItemStack(Blocks.STONE);
        }

        return stack;
    }

    @Override
    public boolean hasSearchBar() {
        return hasSearchBar;
    }
}
