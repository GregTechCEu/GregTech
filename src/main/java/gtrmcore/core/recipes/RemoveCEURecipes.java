package gtrmcore.core.recipes;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;

import net.minecraft.util.ResourceLocation;

import gtrmcore.api.GTRMValues;

public class RemoveCEURecipes {

    public static void init() {
        ModHandler.removeRecipeByName(new ResourceLocation(GTValues.MODID, "workbench_bronze"));
        ModHandler.removeRecipeByName(new ResourceLocation(GTRMValues.MODID_VANILLA, "crafting_table"));
    }
}
