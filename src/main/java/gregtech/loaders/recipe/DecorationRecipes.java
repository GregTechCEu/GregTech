package gregtech.loaders.recipe;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.item.EnumDyeColor;

import static gregtech.api.recipes.RecipeMaps.*;

public class DecorationRecipes {

    private DecorationRecipes() {}

    public static void init() {
        assemblerRecipes();
        dyeRecipes();
    }

    private static void assemblerRecipes() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(OrePrefix.block, Materials.Concrete, 5)
                .inputItem(OrePrefix.plate, Materials.Iron, 2)
                .circuitMeta(8)
                .outputs(MetaBlocks.METAL_SHEET.getItemVariant(EnumDyeColor.WHITE, 32)).volts(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(OrePrefix.block, Materials.Concrete, 5)
                .inputItem(OrePrefix.plate, Materials.Iron, 4)
                .circuitMeta(9)
                .outputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(EnumDyeColor.WHITE, 32)).volts(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(OrePrefix.block, Materials.Concrete, 3)
                .inputItem(OrePrefix.plate, Materials.Rubber, 3)
                .circuitMeta(8)
                .outputs(MetaBlocks.STUDS.getItemVariant(EnumDyeColor.BLACK, 32)).volts(4).duration(20)
                .buildAndRegister();
    }

    private static void dyeRecipes() {
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.METAL_SHEET.getItemVariant(EnumDyeColor.WHITE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.METAL_SHEET.getItemVariant(EnumDyeColor.values()[i])).volts(2).duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(EnumDyeColor.WHITE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(EnumDyeColor.values()[i])).volts(2)
                    .duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.STUDS.getItemVariant(EnumDyeColor.BLACK))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.STUDS.getItemVariant(EnumDyeColor.values()[i])).volts(2).duration(10)
                    .buildAndRegister();
        }
    }
}
