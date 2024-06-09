package gtrmcore.core.recipes;

import gregtech.api.recipes.ModHandler;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import gtrmcore.api.GTRMValues;

public class VanillaOverrideRecipes {

    public static void init() {
        // Flint
        ModHandler.addShapelessRecipe("flint", new ItemStack(Items.FLINT, 1),
                new ItemStack(Blocks.GRAVEL, 1),
                new ItemStack(Blocks.GRAVEL, 1),
                new ItemStack(Blocks.GRAVEL, 1));

        ModHandler.removeRecipeByName(new ResourceLocation(GTRMValues.MODID_VANILLA, "wooden_pickaxe"));
    }
}
