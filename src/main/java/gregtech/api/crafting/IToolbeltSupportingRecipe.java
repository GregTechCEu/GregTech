package gregtech.api.crafting;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.RecipeMatcher;

import org.jetbrains.annotations.NotNull;

public interface IToolbeltSupportingRecipe extends IRecipe {

    ThreadLocal<Boolean> initNeedsToolbeltHandlingHelper = new ThreadLocal<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean toolbeltIngredientCheck(Ingredient ingredient, ItemStack stack) {
        if (stack.getItem() instanceof ItemGTToolbelt toolbelt)
            return toolbelt.supportsIngredient(stack, ingredient);
        else return ingredient.apply(stack);
    }

    @Override
    default @NotNull NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < ret.size(); i++) {
            ret.set(i, inv.getStackInSlot(i));
        }
        int[] matches = RecipeMatcher.findMatches(ret, this.getIngredients());
        if (matches != null) {
            for (int i = 0; i < ret.size(); i++) {
                ItemStack stack = ret.get(i);
                Ingredient ingredient = this.getIngredients().get(matches[i]);
                if (stack.getItem() instanceof ItemGTToolbelt toolbelt) {
                    toolbelt.damageTools(stack, ingredient);
                }
                ret.set(i, ForgeHooks.getContainerItem(stack));
            }
        } else ret.replaceAll(ForgeHooks::getContainerItem);
        return ret;
    }
}
