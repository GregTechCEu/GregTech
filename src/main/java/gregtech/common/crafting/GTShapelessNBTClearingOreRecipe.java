package gregtech.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class GTShapelessNBTClearingOreRecipe extends GTShapelessOreRecipe {
    public GTShapelessNBTClearingOreRecipe(ResourceLocation group, @NotNull ItemStack result, Object... recipe) {
        super(group, result, recipe);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }
}
