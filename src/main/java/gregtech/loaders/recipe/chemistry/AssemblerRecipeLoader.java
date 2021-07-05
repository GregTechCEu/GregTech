package gregtech.loaders.recipe.chemistry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.*;
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
                .outputs(TURBINE_CASING.getItemVariant(BRONZE_GEARBOX))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Steel, 4)
                .input(gear, Steel, 2)
                .input(frameGt, Steel)
                .outputs(TURBINE_CASING.getItemVariant(STEEL_GEARBOX))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Titanium, 4)
                .input(gear, Titanium, 2)
                .input(frameGt, Titanium)
                .outputs(TURBINE_CASING.getItemVariant(TITANIUM_GEARBOX))
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
                .input(pipeMedium, Titanium, 2)
                .inputs(METAL_CASING.getItemVariant(TITANIUM_STABLE))
                .outputs(MULTIBLOCK_CASING.getItemVariant(ENGINE_INTAKE_CASING))
                .duration(100).EUt(30).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 6)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 1))
                .outputs(METAL_CASING.getItemVariant(BRONZE_BRICKS))
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
                .input(plateDouble, Steel, 2)
                .input(ring, Steel, 8)
                .output(LARGE_FLUID_CELL_STEEL)
                .circuitMeta(1)
                .duration(100).EUt(64).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, TungstenSteel, 2)
                .input(ring, TungstenSteel, 8)
                .output(LARGE_FLUID_CELL_TUNGSTEN_STEEL)
                .circuitMeta(1)
                .duration(200).EUt(256).buildAndRegister();

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
    }
}
