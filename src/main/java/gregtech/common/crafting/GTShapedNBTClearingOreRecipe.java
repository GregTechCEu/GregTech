package gregtech.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class GTShapedNBTClearingOreRecipe extends GTShapedOreRecipe {
    public GTShapedNBTClearingOreRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe) {
        super(group, result, recipe);
    }

    @Override
    public @Nonnull NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }
}
