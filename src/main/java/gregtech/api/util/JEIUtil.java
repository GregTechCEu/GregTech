package gregtech.api.util;

import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;

public class JEIUtil {

    /**
     * Check if the player is currently hovering over a valid ingredient for this slot. <br/>
     * Will always return false is JEI is not installed.
     */
    public static boolean hoveringOverIngredient(JeiGhostIngredientSlot<?> jeiGhostIngredientSlot) {
        if (!Mods.JustEnoughItems.isModLoaded()) return false;
        return ModularUIJeiPlugin.hoveringOverIngredient(jeiGhostIngredientSlot);
    }
}
