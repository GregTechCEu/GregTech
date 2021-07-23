package gregtech.loaders.recipe.chemistry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.BRONZE_BRICKS;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TITANIUM_STABLE;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;
import static gregtech.common.items.MetaItems.*;

public class AssemblerRecipeLoader {

    public static void init() {

        // Gearbox-like
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 4)
                .input(gear, Bronze, 2)
                .input(frameGt, Bronze)
                .outputs(TURBINE_CASING.getItemVariant(BRONZE_GEARBOX, 2))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Steel, 4)
                .input(gear, Steel, 2)
                .input(frameGt, Steel)
                .outputs(TURBINE_CASING.getItemVariant(STEEL_GEARBOX, 2))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Titanium, 4)
                .input(gear, Titanium, 2)
                .input(frameGt, Titanium)
                .outputs(TURBINE_CASING.getItemVariant(TITANIUM_GEARBOX, 2))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ROBOT_ARM_IV, 2)
                .input(plate, Steel, 4)
                .input(frameGt, TungstenSteel)
                .outputs(MULTIBLOCK_CASING.getItemVariant(ASSEMBLY_LINE_CASING, 2))
                .duration(100).EUt(30).buildAndRegister();

        // Other
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(rotor, Titanium, 4)
                .input(pipeNormal, Titanium, 2)
                .inputs(METAL_CASING.getItemVariant(TITANIUM_STABLE))
                .outputs(MULTIBLOCK_CASING.getItemVariant(ENGINE_INTAKE_CASING, 2))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 6)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 1))
                .outputs(METAL_CASING.getItemVariant(BRONZE_BRICKS, 2))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Invar, 2)
                .inputs(new ItemStack(Items.FLINT))
                .output(TOOL_LIGHTER_INVAR)
                .duration(256).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Platinum, 2)
                .inputs(new ItemStack(Items.FLINT))
                .output(TOOL_LIGHTER_PLATINUM)
                .duration(256).EUt(256).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(dust, Redstone)
                .input(FLUID_CELL)
                .output(SPRAY_EMPTY)
                .duration(200).EUt(8).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Steel, 4)
                .input(ring, Steel, 8)
                .output(LARGE_FLUID_CELL_STEEL)
                .circuitMeta(1)
                .duration(200).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Aluminium, 4)
                .input(ring, Aluminium, 8)
                .output(LARGE_FLUID_CELL_ALUMINIUM)
                .circuitMeta(1)
                .duration(200).EUt(64).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, StainlessSteel, 4)
                .input(ring, StainlessSteel, 8)
                .output(LARGE_FLUID_CELL_STAINLESS_STEEL)
                .circuitMeta(1)
                .duration(200).EUt(120).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Titanium, 4)
                .input(ring, Titanium, 8)
                .output(LARGE_FLUID_CELL_TITANIUM)
                .circuitMeta(1)
                .duration(200).EUt(256).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, TungstenSteel, 4)
                .input(ring, TungstenSteel, 8)
                .output(LARGE_FLUID_CELL_TUNGSTEN_STEEL)
                .circuitMeta(1)
                .duration(200).EUt(480).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Chrome, 4)
                .input(ring, Chrome, 8)
                .output(LARGE_FLUID_CELL_CHROME)
                .circuitMeta(1)
                .duration(200).EUt(1024).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Iridium, 4)
                .input(ring, Iridium, 8)
                .output(LARGE_FLUID_CELL_IRIDIUM)
                .circuitMeta(1)
                .duration(200).EUt(1920).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Osmium, 4)
                .input(ring, Osmium, 8)
                .output(LARGE_FLUID_CELL_OSMIUM)
                .circuitMeta(1)
                .duration(200).EUt(4096).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Neutronium, 4)
                .input(ring, Neutronium, 8)
                .output(LARGE_FLUID_CELL_NEUTRONIUM)
                .circuitMeta(1)
                .duration(200).EUt(16384).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Tin, 6)
                .input(SPRAY_EMPTY)
                .input(paneGlass.name(), 1)
                .output(FOAM_SPRAYER)
                .duration(200).EUt(8).buildAndRegister();

        // Matches/lighters recipes
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood)
                .input(dustSmall, Phosphorus)
                .output(TOOL_MATCHES)
                .duration(16).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood)
                .input(dustSmall, TricalciumPhosphate)
                .output(TOOL_MATCHES)
                .duration(16).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood, 4)
                .input(dust, Phosphorus)
                .output(TOOL_MATCHES, 4)
                .duration(64).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(bolt, Wood, 4)
                .input(dust, TricalciumPhosphate)
                .output(TOOL_MATCHES, 4)
                .duration(64).EUt(16).buildAndRegister();

        // Wood Pipes
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(30)
                .input(plate, Wood)
                .circuitMeta(12)
                .fluidInputs(Glue.getFluid(60))
                .output(pipeSmall, Wood, 6)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(30)
                .input(plate, Wood, 3)
                .circuitMeta(4)
                .fluidInputs(Glue.getFluid(20))
                .output(pipeNormal, Wood, 4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(30)
                .input(plate, Wood, 3)
                .circuitMeta(2)
                .fluidInputs(Glue.getFluid(10))
                .output(pipeLarge, Wood)
                .buildAndRegister();
    }
}
