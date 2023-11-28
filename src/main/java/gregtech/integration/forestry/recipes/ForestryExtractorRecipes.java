package gregtech.integration.forestry.recipes;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.integration.IntegrationUtil;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import forestry.core.fluids.Fluids;

public class ForestryExtractorRecipes {

    public static void init() {
        // Commonly used items
        ItemStack mulch = IntegrationUtil.getModItem(GTValues.MODID_FR, "mulch", 0);
        ItemStack propolis = IntegrationUtil.getModItem(GTValues.MODID_FR, "propolis", 0);

        // Vanilla Fruit Juice items
        addFruitJuiceRecipe(new ItemStack(Items.CARROT), 200, GTUtility.copy(mulch), 2000);
        addFruitJuiceRecipe(new ItemStack(Items.APPLE), 200, GTUtility.copy(mulch), 2000);

        // Forestry fruits
        addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 0), 50, GTUtility.copy(mulch), 500);
        addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 1), 180, GTUtility.copy(mulch), 500);
        addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 2), 220, GTUtility.copy(mulch), 200);
        addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 3), 400, GTUtility.copy(mulch),
                1000);
        addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 4), 100, GTUtility.copy(mulch),
                6000);
        addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 5), 50, GTUtility.copy(mulch),
                2000);
        addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "fruits", 6), 600, GTUtility.copy(mulch),
                1000);

        // Honey, Honeydew
        addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "honey_drop", 0), 100, GTUtility.copy(propolis),
                0);
        addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_FR, "honeydew", 0), 100);

        if (Loader.isModLoaded(GTValues.MODID_EB)) {
            // Propolis
            addExtractorRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 0),
                    Materials.Water.getFluid(500));
            addExtractorRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 1),
                    Materials.Oil.getFluid(500));
            addExtractorRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "propolis", 2),
                    Materials.Creosote.getFluid(500));

            // Drops
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 3), 200);
            addExtractorRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 6),
                    Fluids.MILK.getFluid(200));
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 13), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 19), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 14), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 20), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 15), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 21), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 16), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 22), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 17), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 24), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 18), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 23), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 19), 200,
                    IntegrationUtil.getModItem(GTValues.MODID_EB, "misc", 25), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 20), 200,
                    new ItemStack(Items.DYE, 1, 14), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 21), 200,
                    new ItemStack(Items.DYE, 1, 6), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 22), 200,
                    new ItemStack(Items.DYE, 1, 5), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 23), 200,
                    new ItemStack(Items.DYE, 1, 8), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 24), 200,
                    new ItemStack(Items.DYE, 1, 12), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 25), 200,
                    new ItemStack(Items.DYE, 1, 9), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 26), 200,
                    new ItemStack(Items.DYE, 1, 10), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 27), 200,
                    new ItemStack(Items.DYE, 1, 13), 0);
            addHoneyRecipe(IntegrationUtil.getModItem(GTValues.MODID_EB, "honey_drop", 28), 200,
                    new ItemStack(Items.DYE, 1, 7), 0);
        }

        if (Loader.isModLoaded(GTValues.MODID_ET)) {

            // Fruits
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 0), 150, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 1), 400, GTUtility.copy(mulch),
                    1500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 2), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 3), 300, GTUtility.copy(mulch),
                    1000);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 4), 50, GTUtility.copy(mulch), 500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 5), 50, GTUtility.copy(mulch), 300);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 6), 50, GTUtility.copy(mulch), 500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 7), 50, GTUtility.copy(mulch),
                    500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 8), 100, GTUtility.copy(mulch),
                    6000);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 9), 80, GTUtility.copy(mulch), 500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 10), 150, GTUtility.copy(mulch),
                    4000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 11), 500, GTUtility.copy(mulch),
                    1500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 12), 150, GTUtility.copy(mulch),
                    4000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 13), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 14), 400, GTUtility.copy(mulch),
                    1500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 15), 400, GTUtility.copy(mulch),
                    1500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 16), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 17), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 18), 400, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 19), 150, GTUtility.copy(mulch),
                    4000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 20), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 21), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 22), 300, GTUtility.copy(mulch),
                    2000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 23), 200, GTUtility.copy(mulch),
                    1000);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 24), 150, GTUtility.copy(mulch),
                    500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 25), 180, GTUtility.copy(mulch),
                    500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 26), 100, GTUtility.copy(mulch),
                    400);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 27), 50, GTUtility.copy(mulch), 200);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 28), 100, GTUtility.copy(mulch),
                    3000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 29), 100, GTUtility.copy(mulch),
                    3000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 30), 100, GTUtility.copy(mulch),
                    4000);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 31), 20, GTUtility.copy(mulch), 200);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 32), 50, GTUtility.copy(mulch), 300);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 33), 50, GTUtility.copy(mulch), 300);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 34), 100, GTUtility.copy(mulch),
                    500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 35), 50, GTUtility.copy(mulch), 300);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 36), 50, GTUtility.copy(mulch), 500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 37), 20, GTUtility.copy(mulch), 200);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 38), 300, GTUtility.copy(mulch),
                    1500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 39), 25, GTUtility.copy(mulch), 200);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 46), 50, GTUtility.copy(mulch),
                    500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 50), 300, GTUtility.copy(mulch),
                    2500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 51), 150, GTUtility.copy(mulch),
                    1500);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 52), 300, GTUtility.copy(mulch),
                    1500);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 53), 50, GTUtility.copy(mulch),
                    1000);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 54), 50, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 55), 100, GTUtility.copy(mulch),
                    1000);
            addSeedOilRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 56), 100, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 57), 400, GTUtility.copy(mulch),
                    2000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 58), 300, GTUtility.copy(mulch),
                    1000);
            addFruitJuiceRecipe(IntegrationUtil.getModItem(GTValues.MODID_ET, "food", 59), 50, GTUtility.copy(mulch),
                    1000);
        }
    }

    private static void addSeedOilRecipe(ItemStack inputStack, int outputAmount) {
        addExtractorRecipe(inputStack, Materials.SeedOil.getFluid(outputAmount), ItemStack.EMPTY, 0);
    }

    private static void addSeedOilRecipe(ItemStack inputStack, int outputAmount, ItemStack extraOutput, int chance) {
        addExtractorRecipe(inputStack, Materials.SeedOil.getFluid(outputAmount), extraOutput, chance);
    }

    private static void addHoneyRecipe(ItemStack inputStack, int outputAmount) {
        addExtractorRecipe(inputStack, Fluids.FOR_HONEY.getFluid(outputAmount), ItemStack.EMPTY, 0);
    }

    private static void addHoneyRecipe(ItemStack inputStack, int outputAmount, ItemStack extraOutput, int chance) {
        addExtractorRecipe(inputStack, Fluids.FOR_HONEY.getFluid(outputAmount), extraOutput, chance);
    }

    private static void addFruitJuiceRecipe(ItemStack inputStack, int outputAmount) {
        addExtractorRecipe(inputStack, Fluids.JUICE.getFluid(outputAmount), ItemStack.EMPTY, 0);
    }

    private static void addFruitJuiceRecipe(ItemStack inputStack, int outputAmount, ItemStack extraOutput, int chance) {
        addExtractorRecipe(inputStack, Fluids.JUICE.getFluid(outputAmount), extraOutput, chance);
    }

    private static void addExtractorRecipe(ItemStack inputStack, FluidStack outputFluid) {
        addExtractorRecipe(inputStack, outputFluid, ItemStack.EMPTY, 0);
    }

    private static void addExtractorRecipe(ItemStack inputStack, FluidStack outputFluid, ItemStack extraOutput,
                                           int chance) {
        RecipeBuilder<?> builder = RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(inputStack)
                .fluidOutputs(outputFluid);

        if (extraOutput != ItemStack.EMPTY) {
            if (chance > 0) {
                builder.chancedOutput(extraOutput, chance, 0);
            } else {
                builder.outputs(extraOutput);
            }
        }

        builder.duration(32).EUt(7).buildAndRegister();
    }
}
