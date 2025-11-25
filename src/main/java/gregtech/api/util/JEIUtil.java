package gregtech.api.util;

import gregtech.integration.jei.JustEnoughItemsModule;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;

public class JEIUtil {

    /**
     * Check if the player is currently hovering over a valid ingredient for this slot. <br/>
     * Will always return false is JEI is not installed.
     */
    public static boolean hoveringOverIngredient(RecipeViewerGhostIngredientSlot<?> jeiGhostIngredientSlot) {
        if (!Mods.JustEnoughItems.isModLoaded()) return false;
        return ModularUIJeiPlugin.hoveringOverIngredient(jeiGhostIngredientSlot);
    }

    public static ItemStack getActualStack(Object ingredient) {
        if (!Mods.JustEnoughItems.isModLoaded()) return ItemStack.EMPTY;
        return JustEnoughItemsModule.ingredientRegistry
                .getIngredientHelper(ingredient)
                .getCheatItemStack(ingredient);
    }
}
