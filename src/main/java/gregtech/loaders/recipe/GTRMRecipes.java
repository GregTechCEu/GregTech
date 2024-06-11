package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.common.items.gtrmcore.GTRMItems;
import gregtech.common.metatileentities.gtrmcore.GTRMMetaBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class GTRMRecipes {

    public static void init() {
        // materials();
        items();
        blocks();
        // tools();
        // end_contents();
    }

    private static void items() {
        // クラフトレシピ
        /*
         * ModHandler.addShapedRecipe(true, "meta_item",
         * GTRMMetaItems.WOOD_FIBER.getStackForm(), "AnY",
         * '', "");
         */
        ModHandler.addShapedRecipe(true, "wood_fiber",
                MetaItems.WOOD_FIBER.getStackForm(), "kS",
                'S', "treeSapling");
        ModHandler.addShapedRecipe(true, "wooden_pickaxe_head",
                MetaItems.WOODEN_PICKAXE_HEAD.getStackForm(), "PP", "kP",
                'P', "plankWood");
        ModHandler.addShapedRecipe(true, "wooden_pickaxe",
                Items.WOODEN_PICKAXE.getDefaultInstance(), "H ", "SF",
                'H', MetaItems.WOODEN_PICKAXE_HEAD,
                'S', Items.STICK,
                'F', MetaItems.WOOD_FIBER);
        ModHandler.addShapedRecipe(true, "cobblestone_saw_head",
                MetaItems.COBBLESTONE_SAW_HEAD.getStackForm(), "C", "C",
                'C', GTRMMetaBlocks.COMPACT_COBBLESTONE);

        ModHandler.addShapedRecipe(true, "wooden_hard_hammer_head",
                MetaItems.WOODEN_HARD_HAMMER_HEAD.getStackForm(), " P", "SP",
                'P', "plankWood",
                'S', GTRMItems.COBBLESTONE_SAW);
        ModHandler.addShapedRecipe(true, "wood_hard_hammer",
                GTRMItems.WOODEN_HARD_HAMMER.getDefaultInstance(), "H ", "SF",
                'H', MetaItems.WOODEN_HARD_HAMMER_HEAD,
                'S', Items.STICK,
                'F', MetaItems.WOOD_FIBER);
        ModHandler.addShapedRecipe(true, "cobblestone_saw",
                GTRMItems.COBBLESTONE_SAW.getDefaultInstance(), "H ", "SF",
                'H', MetaItems.COBBLESTONE_SAW_HEAD,
                'S', Items.STICK,
                'F', MetaItems.WOOD_FIBER);
    }

    private static void blocks() {
        ModHandler.addShapedRecipe(true, "compact_cobblestone",
                GTRMMetaBlocks.COMPACT_COBBLESTONE.getItemStack(), "CC", "CC",
                'C', Item.getItemFromBlock(Blocks.COBBLESTONE));
    }

    private static void recipeCutter(ItemStack input, ItemStack output) {
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidInputs(Materials.Lubricant.getFluid(1))
                .outputs(GTUtility.copy(6, output))
                .output(dust, Materials.Wood, 2)
                .duration(200).EUt(VA[ULV])
                .buildAndRegister();
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidInputs(Materials.DistilledWater.getFluid(3))
                .outputs(GTUtility.copy(6, output))
                .output(dust, Materials.Wood, 2)
                .duration(300).EUt(VA[ULV])
                .buildAndRegister();
        RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidInputs(Materials.Water.getFluid(4))
                .outputs(GTUtility.copy(6, output))
                .output(dust, Materials.Wood, 2)
                .duration(400).EUt(VA[ULV])
                .buildAndRegister();
    }
}
