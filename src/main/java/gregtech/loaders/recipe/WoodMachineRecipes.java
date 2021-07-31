package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.ConfigHolder;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.BIO_CHAFF;

public class WoodMachineRecipes {

    public static void init() {
        initializeWoodRecipes();
        registerPyrolyseOvenRecipes();
    }

    public static void postInit() {
        processLogOreDictionary();
    }

    private static void initializeWoodRecipes() {

        MACERATOR_RECIPES.recipeBuilder()
                .input(log, Wood)
                .output(dust, Wood, 6)
                .chancedOutput(dust, Wood, 8000, 680)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .input(plank, Wood)
                .output(stick, Wood, 2)
                .duration(10).EUt(8)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .input(log, Wood)
                .output(stickLong, Wood, 4)
                .output(dust, Wood, 2)
                .duration(160).EUt(8)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.SAPLING, 1, GTValues.W))
                .outputs(new ItemStack(Items.STICK))
                .output(dustTiny, Wood)
                .duration(16).EUt(8)
                .buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W))
                .outputs(new ItemStack(Items.BOWL))
                .output(dustSmall, Wood)
                .duration(50).EUt(8)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plank, Wood, 6)
                .inputs(new ItemStack(Items.BOOK, 3))
                .outputs(new ItemStack(Blocks.BOOKSHELF))
                .duration(400).EUt(4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plank, Wood, 3).circuitMeta(3)
                .outputs(new ItemStack(Blocks.TRAPDOOR, 2))
                .duration(300).EUt(4)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plank, Wood, 8)
                .outputs(new ItemStack(Blocks.CHEST))
                .duration(800).EUt(4).circuitMeta(8)
                .buildAndRegister();
    }

    private static void processLogOreDictionary() {
        List<ItemStack> allWoodLogs = OreDictUnifier.getAllWithOreDictionaryName("logWood").stream()
                .flatMap(stack -> ModHandler.getAllSubItems(stack).stream())
                .collect(Collectors.toList());

        for (ItemStack stack : allWoodLogs) {
            Pair<IRecipe, ItemStack> outputPair = ModHandler.getRecipeOutput(null, stack);
            ItemStack plankStack = outputPair.getValue();
            int originalOutput = plankStack.getCount();
            if (plankStack.isEmpty()) {
                continue;
            }
            IRecipe outputRecipe = outputPair.getKey();
            if (ConfigHolder.vanillaRecipes.nerfWoodCrafting) {
                if (originalOutput / 2 > 0) {
                    //noinspection ConstantConditions
                    ModHandler.addShapelessRecipe(outputRecipe.getRegistryName().toString(),
                            GTUtility.copyAmount(originalOutput / 2, plankStack), stack);
                } else {
                    //if not enough planks are on output, just remove recipe
                    ModHandler.removeRecipeByName(outputRecipe.getRegistryName());
                }
            }
            //noinspection ConstantConditions
            ModHandler.addShapedRecipe(outputRecipe.getRegistryName().getPath() + "_saw",
                    GTUtility.copyAmount(originalOutput, plankStack), "s", "L", 'L', stack);

            CUTTER_RECIPES.recipeBuilder().inputs(stack)
                    .fluidInputs(Lubricant.getFluid(1))
                    .outputs(GTUtility.copyAmount((int) (originalOutput * 1.5), plankStack), OreDictUnifier.get(dust, Wood, 2))
                    .duration(200).EUt(8)
                    .buildAndRegister();

            ItemStack doorStack = ModHandler.getRecipeOutput(DummyWorld.INSTANCE,
                    plankStack, plankStack, null,
                    plankStack, plankStack, null,
                    plankStack, plankStack, null).getRight();

            if (!doorStack.isEmpty()) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(6, plankStack))
                        .outputs(doorStack)
                        .duration(600).EUt(4).circuitMeta(6)
                        .buildAndRegister();
            }

            ItemStack slabStack = ModHandler.getRecipeOutput(DummyWorld.INSTANCE, plankStack, plankStack, plankStack).getRight();

            if (!slabStack.isEmpty()) {
                CUTTER_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(3, plankStack))
                        .outputs(slabStack)
                        .duration(200).EUt(8)
                        .buildAndRegister();
            }
        }
    }

    private static void registerPyrolyseOvenRecipes() {
        // Logs ================================================

        // Charcoal Byproducts
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(4)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(CharcoalByproducts.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Wood Tar
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(9)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodTar.getFluid(1500))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(10)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodTar.getFluid(1500))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Wood Gas
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(5)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodGas.getFluid(1500))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(6)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodGas.getFluid(1500))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Wood Vinegar
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(7)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodVinegar.getFluid(3000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(WoodVinegar.getFluid(3000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(log, Wood, 16)
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(Creosote.getFluid(4000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(log, Wood, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(Creosote.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // Heavy Oil
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(3)
                .input(log, Wood, 16)
                .output(dust, Ash, 4)
                .fluidOutputs(OilHeavy.getFluid(200))
                .duration(320).EUt(192)
                .buildAndRegister();

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(gem, Coal, 16)
                .output(gem, Coke, 16)
                .fluidOutputs(Creosote.getFluid(8000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(gem, Coal, 16)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(gem, Coke, 16)
                .fluidOutputs(Creosote.getFluid(8000))
                .duration(320).EUt(96)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(block, Coal, 8)
                .output(block, Coke, 8)
                .fluidOutputs(Creosote.getFluid(32000))
                .duration(2560).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(block, Coal, 8)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(block, Coke, 8)
                .fluidOutputs(Creosote.getFluid(32000))
                .duration(1280).EUt(96)
                .buildAndRegister();

        // Biomass
        PYROLYSE_RECIPES.recipeBuilder().EUt(10).duration(200)
                .input(BIO_CHAFF)
                .circuitMeta(2)
                .fluidInputs(Water.getFluid(1500))
                .fluidOutputs(FermentedBiomass.getFluid(1500))
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().EUt(10).duration(900)
                .input(BIO_CHAFF, 4)
                .circuitMeta(1)
                .fluidInputs(Water.getFluid(4000))
                .fluidOutputs(Biomass.getFluid(5000))
                .buildAndRegister();

        // Sugar to Charcoal
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
                .input(dust, Sugar, 23)
                .output(dust, Charcoal, 12)
                .fluidOutputs(Water.getFluid(1500))
                .duration(320).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
                .input(dust, Sugar, 23)
                .fluidInputs(Nitrogen.getFluid(500))
                .output(dust, Charcoal, 12)
                .fluidOutputs(Water.getFluid(1500))
                .duration(160).EUt(96)
                .buildAndRegister();

        // COAL GAS ============================================

        // From Log
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(20)
                .input(log, Wood, 16)
                .fluidInputs(Steam.getFluid(1000))
                .outputs(new ItemStack(Items.COAL, 20, 1))
                .fluidOutputs(CoalGas.getFluid(2000))
                .duration(640).EUt(64)
                .buildAndRegister();

        // From Coal
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(22)
                .input(gem, Coal, 16)
                .fluidInputs(Steam.getFluid(1000))
                .output(gem, Coke, 16)
                .fluidOutputs(CoalGas.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();

        // COAL TAR ============================================
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .inputs(new ItemStack(Items.COAL, 32, 1))
                .output(dustSmall, Ash, 2)
                .fluidOutputs(CoalTar.getFluid(1000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .input(gem, Lignite, 16)
                .output(dustSmall, DarkAsh, 2)
                .fluidOutputs(CoalTar.getFluid(2000))
                .duration(640).EUt(64)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .inputs(new ItemStack(Items.COAL, 12))
                .output(dustSmall, DarkAsh, 2)
                .fluidOutputs(CoalTar.getFluid(3000))
                .duration(320).EUt(96)
                .buildAndRegister();

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .input(gem, Coke, 8)
                .output(dustSmall, Ash, 3)
                .fluidOutputs(CoalTar.getFluid(4000))
                .duration(320).EUt(96)
                .buildAndRegister();
    }
}
