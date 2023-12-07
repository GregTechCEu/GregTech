package gregtech.integration.forestry.recipes;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.integration.forestry.ForestryModule;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ForestryToolRecipes {

    public static void registerHandlers() {
        OrePrefix.stick.addProcessingHandler(PropertyKey.TOOL, ForestryToolRecipes::processScoop);
    }

    private static void processScoop(OrePrefix prefix, Material material, ToolProperty property) {
        ToolRecipeHandler.addToolRecipe(material, ForestryModule.SCOOP, false,
                "SWS", "SSS", "xSh",
                'S', new UnificationEntry(OrePrefix.stick, material),
                'W', new ItemStack(Blocks.WOOL, 1, GTValues.W));
    }
}
