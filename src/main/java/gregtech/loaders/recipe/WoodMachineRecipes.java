package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
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

public class WoodMachineRecipes {

    public static void init() {
        initializeWoodRecipes();
        registerPyrolyseOvenRecipes();
    }

    public static void postInit() {
        processLogOreDictionary();
    }

    private static void initializeWoodRecipes() {
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .input(OrePrefix.log, Materials.Wood)
            .outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Wood, 6))
            .chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Wood), 8000, 680)
            .buildAndRegister();

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
            .input(OrePrefix.plank, Materials.Wood)
            .outputs(OreDictUnifier.get(OrePrefix.stick, Materials.Wood, 2))
            .duration(10).EUt(8)
            .buildAndRegister();

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
            .input(OrePrefix.log, Materials.Wood)
            .outputs(OreDictUnifier.get(OrePrefix.stickLong, Materials.Wood, 4),
                OreDictUnifier.get(OrePrefix.dust, Materials.Wood, 2))
            .duration(160).EUt(8)
            .buildAndRegister();

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.SAPLING, 1, GTValues.W))
            .outputs(new ItemStack(Items.STICK))
            .output(OrePrefix.dustTiny, Materials.Wood)
            .duration(16).EUt(8)
            .buildAndRegister();

        RecipeMaps.LATHE_RECIPES.recipeBuilder()
            .inputs(new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W))
            .outputs(new ItemStack(Items.BOWL))
            .output(OrePrefix.dustSmall, Materials.Wood)
            .duration(50).EUt(8)
            .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .input(OrePrefix.plank, Materials.Wood, 6)
            .inputs(new ItemStack(Items.BOOK, 3))
            .outputs(new ItemStack(Blocks.BOOKSHELF, 1))
            .duration(400).EUt(4)
            .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .input(OrePrefix.plank, Materials.Wood, 3).circuitMeta(3)
            .outputs(new ItemStack(Blocks.TRAPDOOR, 2))
            .duration(300).EUt(4)
            .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
            .input(OrePrefix.plank, Materials.Wood, 8)
            .outputs(new ItemStack(Blocks.CHEST, 1))
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
                //noinspection ConstantConditions
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

            RecipeMaps.CUTTER_RECIPES.recipeBuilder().inputs(stack)
                .fluidInputs(Materials.Lubricant.getFluid(1))
                .outputs(GTUtility.copyAmount((int) (originalOutput * 1.5), plankStack), OreDictUnifier.get(OrePrefix.dust, Materials.Wood, 2))
                .duration(200).EUt(8)
                .buildAndRegister();

            ItemStack doorStack = ModHandler.getRecipeOutput(DummyWorld.INSTANCE,
                plankStack, plankStack, null,
                plankStack, plankStack, null,
                plankStack, plankStack, null).getRight();

            if (!doorStack.isEmpty()) {
                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copyAmount(6, plankStack))
                    .outputs(doorStack)
                    .duration(600).EUt(4).circuitMeta(6)
                    .buildAndRegister();
            }

            ItemStack slabStack = ModHandler.getRecipeOutput(DummyWorld.INSTANCE, plankStack, plankStack, plankStack).getRight();

            if (!slabStack.isEmpty()) {
                RecipeMaps.CUTTER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copyAmount(3, plankStack))
                    .outputs(slabStack)
                    .duration(200).EUt(8)
                    .buildAndRegister();
            }
        }
    }

    private static void registerPyrolyseOvenRecipes() {
        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(0)
            .input(OrePrefix.gem, Materials.Coal, 16)
            .outputs(OreDictUnifier.get(OrePrefix.gem, Materials.Coke, 20))
            .fluidOutputs(Materials.Creosote.getFluid(10000))
            .duration(440).EUt(96)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(0)
            .input(OrePrefix.log, Materials.Wood, 16)
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.Creosote.getFluid(4000))
            .duration(440).EUt(64)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(1)
            .input(OrePrefix.log, Materials.Wood, 16)
            .fluidInputs(Materials.Nitrogen.getFluid(400))
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.Creosote.getFluid(4000))
            .duration(200).EUt(96)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(2)
            .input(OrePrefix.log, Materials.Wood, 16)
            .outputs(OreDictUnifier.get(OrePrefix.dust, Materials.Ash, 4))
            .fluidOutputs(Materials.OilHeavy.getFluid(200))
            .duration(280).EUt(192)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(3)
            .input(OrePrefix.log, Materials.Wood, 16)
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.WoodVinegar.getFluid(3000))
            .duration(640).EUt(64)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(4)
            .input(OrePrefix.log, Materials.Wood, 16)
            .fluidInputs(Materials.Nitrogen.getFluid(400))
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.WoodVinegar.getFluid(3000))
            .duration(320).EUt(96)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(5)
            .input(OrePrefix.log, Materials.Wood, 16)
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.WoodGas.getFluid(1500))
            .duration(640).EUt(64)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(6)
            .input(OrePrefix.log, Materials.Wood, 16)
            .fluidInputs(Materials.Nitrogen.getFluid(400))
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.WoodGas.getFluid(1500))
            .duration(320).EUt(96)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(7)
            .input(OrePrefix.log, Materials.Wood, 16)
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.WoodTar.getFluid(1500))
            .duration(640).EUt(64)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
            .input(OrePrefix.log, Materials.Wood, 16)
            .fluidInputs(Materials.Nitrogen.getFluid(400))
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.WoodTar.getFluid(1500))
            .duration(320).EUt(96)
            .buildAndRegister();

        RecipeMaps.PYROLYSE_RECIPES.recipeBuilder().circuitMeta(9)
            .input(OrePrefix.log, Materials.Wood, 16)
            .fluidInputs(Materials.Nitrogen.getFluid(400))
            .outputs(new ItemStack(Items.COAL, 20, 1))
            .fluidOutputs(Materials.CharcoalByproducts.getFluid(4000))
            .duration(320).EUt(96)
            .buildAndRegister();
    }
}
