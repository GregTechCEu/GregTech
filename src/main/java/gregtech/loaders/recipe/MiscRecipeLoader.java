package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.common.items.MetaItems.TOOL_MATCHBOX;
import static gregtech.common.items.MetaItems.TOOL_MATCHES;

public class MiscRecipeLoader {

    public static void init() {

        // Potin Recipe
        ModHandler.addShapelessRecipe("potin_dust", OreDictUnifier.get(dust, Potin, 5),
                new UnificationEntry(dust, Lead),
                new UnificationEntry(dust, Lead),
                new UnificationEntry(dust, Bronze),
                new UnificationEntry(dust, Bronze),
                new UnificationEntry(dust, Tin));

        // Mixed Metal Ingots
        final MaterialStack[] firstMetal = {
                new MaterialStack(Materials.Iron, 1),
                new MaterialStack(Materials.Nickel, 1),
                new MaterialStack(Materials.Invar, 2),
                new MaterialStack(Materials.Steel, 2),
                new MaterialStack(Materials.StainlessSteel, 3),
                new MaterialStack(Materials.Titanium, 3),
                new MaterialStack(Materials.Tungsten, 4),
                new MaterialStack(Materials.TungstenSteel, 5)
        };

        final MaterialStack[] lastMetal = {
                new MaterialStack(Materials.Tin, 0),
                new MaterialStack(Materials.Zinc, 0),
                new MaterialStack(Materials.Aluminium, 1)
        };

        int multiplier;
        for (MaterialStack metal1 : firstMetal) {
            Material material1 = metal1.material;
            int multiplier1 = (int) metal1.amount;
            for (MaterialStack metal2 : lastMetal) {
                Material material2 = metal2.material;
                if ((int) metal1.amount == 1) multiplier = 0;
                else multiplier = (int) metal2.amount;
                ModHandler.addShapedRecipe("mixed_metal_1_" + material1.toString() + "_" + material2.toString(), MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier),
                        "F", "M", "L",
                        'F', new UnificationEntry(OrePrefix.plate, material1),
                        'M', new UnificationEntry(OrePrefix.plate, Bronze),
                        'L', new UnificationEntry(OrePrefix.plate, material2));

                ModHandler.addShapedRecipe("mixed_metal_2_" + material1.toString() + "_" + material2.toString(), MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier),
                        "F", "M", "L",
                        'F', new UnificationEntry(OrePrefix.plate, material1),
                        'M', new UnificationEntry(OrePrefix.plate, Brass),
                        'L', new UnificationEntry(OrePrefix.plate, material2));

                FORMING_PRESS_RECIPES.recipeBuilder().duration(40 * multiplier1 + multiplier * 40).EUt(8)
                        .input(OrePrefix.plate, material1)
                        .input(OrePrefix.plank, Bronze)
                        .input(OrePrefix.plate, material2)
                        .outputs(MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier))
                        .buildAndRegister();

                FORMING_PRESS_RECIPES.recipeBuilder().duration(40 * multiplier1 + multiplier * 40).EUt(8)
                        .input(OrePrefix.plate, material1)
                        .input(OrePrefix.plate, Brass)
                        .input(OrePrefix.plate, material2)
                        .outputs(MetaItems.INGOT_MIXED_METAL.getStackForm(multiplier1 + multiplier))
                        .buildAndRegister();
            }
        }

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(8)
                .input(dust, Sugar)
                .inputs(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(8)
                .input(dust, Sugar)
                .inputs(new ItemStack(Blocks.RED_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        RecipeMaps.SIFTER_RECIPES.recipeBuilder().duration(800).EUt(16)
                .inputs(new ItemStack(Blocks.GRAVEL))
                .outputs(new ItemStack(Items.FLINT))
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(TOOL_MATCHES.getStackForm(16)).input(OrePrefix.plate, Materials.Paper)
                .outputs(TOOL_MATCHBOX.getStackForm())
                .duration(64)
                .EUt(16)
                .buildAndRegister();
    }
}
