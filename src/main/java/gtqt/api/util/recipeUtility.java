package gtqt.api.util;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.VA;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING;

public class recipeUtility {

    public static void registerMachineCasingRecipes(Material material, ItemStack casing, int tier) {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[tier])
                .input(OrePrefix.plate, material, 8)
                .outputs(casing)
                .circuitMeta(8)
                .duration(50)
                .buildAndRegister();

        ModHandler.addShapedRecipe(true, casing.getTranslationKey(), casing,
                "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, material));
    }

    public static void registerCasingRecipes(Material material, ItemStack casing, int tier) {
        registerCasingRecipes(material, material, casing, tier);
    }

    public static void registerCasingRecipes(Material materialPlate, Material materialFrame, ItemStack casing,
                                             int tier) {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, materialPlate, 6)
                .input(frameGt, materialFrame, 1)
                .circuitMeta(6)
                .outputs(GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing))
                .EUt(VA[tier])
                .duration(50)
                .buildAndRegister();

        ModHandler.addShapedRecipe(true, casing.getTranslationKey(),
                GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(OrePrefix.plate, materialPlate),
                'F', new UnificationEntry(OrePrefix.frameGt, materialFrame));
    }

    public static void registerFireboxRecipes(Material material, ItemStack casing) {
        ModHandler.addShapedRecipe(true, casing.getTranslationKey(),
                GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing),
                "PSP", "SFS", "PSP", 'P', new UnificationEntry(OrePrefix.plate, material), 'F',
                new UnificationEntry(OrePrefix.frameGt, material), 'S',
                new UnificationEntry(OrePrefix.stick, material));
    }

    public static void registerGearboxesRecipes(Material material, ItemStack casing) {
        ModHandler.addShapedRecipe(true, casing.getTranslationKey(),
                GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing), "PhP",
                "GFG", "PwP", 'P', new UnificationEntry(OrePrefix.plate, material), 'F',
                new UnificationEntry(OrePrefix.frameGt, material), 'G',
                new UnificationEntry(OrePrefix.gear, material));
    }

    public static void registerPipeRecipes(Material material, ItemStack casing) {
        ModHandler.addShapedRecipe(true, casing.getTranslationKey(),
                GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing), "PIP",
                "IFI", "PIP", 'P', new UnificationEntry(OrePrefix.plate, material), 'F',
                new UnificationEntry(OrePrefix.frameGt, material), 'I',
                new UnificationEntry(OrePrefix.pipeNormalFluid, material));
    }

    public static void registerTurbineRecipes(Material material, ItemStack casing, int tier) {
        if (material == Materials.Magnalium) {
            ModHandler.addShapedRecipe(true, casing.getTranslationKey(),
                    GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing),
                    "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, material), 'F',
                    new UnificationEntry(OrePrefix.frameGt, Materials.BlueSteel));

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[tier])
                    .input(OrePrefix.plate, Materials.Magnalium, 6)
                    .input(OrePrefix.frameGt, Materials.BlueSteel, 1)
                    .circuitMeta(6)
                    .outputs(GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing))
                    .duration(50)
                    .buildAndRegister();
        } else {
            ModHandler.addShapedRecipe(true, casing.getTranslationKey(),
                    GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing),
                    "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, material), 'F',
                    MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_TURBINE_CASING));

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[tier])
                    .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(
                            BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING))
                    .input(OrePrefix.plate, material, 6)
                    .circuitMeta(6)
                    .outputs(GTUtility.copy(ConfigHolder.recipes.casingsPerCraft, casing))
                    .duration(50).buildAndRegister();
        }
    }

    public static void registerDustRecipes(Material material, int tier) {
        int amount = 0;
        // generate builder
        RecipeBuilder<?> builder;

        builder = RecipeMaps.MIXER_RECIPES.recipeBuilder();

        for (MaterialStack component : material.getMaterialComponents()) {
            amount += (int) component.amount;
            builder.input(dust, component.material, (int) component.amount);
        }

        builder.circuitMeta(amount % 10)
                .output(dust, material, amount)
                .EUt(VA[tier])
                .duration(10 * amount * tier)
                .buildAndRegister();
    }
}
