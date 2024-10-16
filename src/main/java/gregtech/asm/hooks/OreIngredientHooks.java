package gregtech.asm.hooks;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreIngredient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class OreIngredientHooks {

    public static boolean checkToolbelt(@Nullable ItemStack input, @NotNull OreIngredient ingredient) {
        if (input.getItem() instanceof ItemGTToolbelt toolbelt) {
            if (toolbelt.supportsIngredient(input, ingredient)) {
                toolbelt.setOnCraftIngredient(input, ingredient);
                return true;
            }
        }
        return false;
    }
}
