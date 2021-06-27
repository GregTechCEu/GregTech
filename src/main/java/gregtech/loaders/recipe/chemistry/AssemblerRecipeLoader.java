package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.*;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class AssemblerRecipeLoader {

    public static void init() {

        // Gearbox-like
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 4)
                .input(gear, Bronze, 2)
                .input(frameGt, Bronze)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(BlockTurbineCasing.TurbineCasingType.BRONZE_GEARBOX))
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Steel, 4)
                .input(gear, Steel, 2)
                .input(frameGt, Steel)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX))
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Titanium, 4)
                .input(gear, Titanium, 2)
                .input(frameGt, Titanium)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX))
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaItems.ROBOT_ARM_IV.getStackForm(2), OreDictUnifier.get(plate, Materials.Steel, 4), OreDictUnifier.get(OrePrefix.frameGt, Materials.TungstenSteel))
                .outputs(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING, 2))
                .duration(100).EUt(30).buildAndRegister();

        // Other
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(rotor, Titanium, 4)
                .input(pipeMedium, Titanium, 2)
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE))
                .outputs(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING))
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Bronze, 6)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 1))
                .outputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS))
                .duration(100).EUt(30).buildAndRegister();
    }
}
