package gregtech.loaders.recipe;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.BlockLargeMetalSheet;
import gregtech.common.blocks.BlockMetalSheet;
import gregtech.common.blocks.BlockStuds;
import gregtech.common.blocks.MetaBlocks;

import static gregtech.api.recipes.RecipeMaps.*;
public class DecorationRecipes {

    private DecorationRecipes(){}
    public static void init() {
        assemblerRecipes();
        dyeRecipes();
    }

    private static void assemblerRecipes() {

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.block, Materials.Concrete, 5)
                .input(OrePrefix.plate, Materials.Iron, 2)
                .circuitMeta(8)
                .outputs(MetaBlocks.METAL_SHEET.getItemVariant(BlockMetalSheet.SheetType.WHITE, 32))
                .EUt(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.block, Materials.Concrete, 5)
                .input(OrePrefix.plate, Materials.Iron, 4)
                .circuitMeta(9)
                .outputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(BlockLargeMetalSheet.SheetType.WHITE, 32))
                .EUt(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.block, Materials.Concrete, 3)
                .input(OrePrefix.plate, Materials.Rubber, 3)
                .circuitMeta(8)
                .outputs(MetaBlocks.STUDS.getItemVariant(BlockStuds.StudsType.BLACK, 32))
                .EUt(4).duration(20)
                .buildAndRegister();

    }

    private static void dyeRecipes() {

        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.METAL_SHEET.getItemVariant(BlockMetalSheet.SheetType.WHITE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.METAL_SHEET.getItemVariant(BlockMetalSheet.SheetType.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(BlockLargeMetalSheet.SheetType.WHITE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(BlockLargeMetalSheet.SheetType.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.STUDS.getItemVariant(BlockStuds.StudsType.BLACK))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.STUDS.getItemVariant(BlockStuds.StudsType.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();
        }

    }

}
