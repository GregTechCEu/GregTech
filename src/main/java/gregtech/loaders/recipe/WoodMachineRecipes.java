package gregtech.loaders.recipe;

import com.google.common.collect.ImmutableMap;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregPlanks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Map;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.BIO_CHAFF;

public class WoodMachineRecipes {

    public static void init() {
        registerWoodRecipes();
        registerPyrolyseOvenRecipes();
    }

    private static void registerWoodRecipes() {
        ImmutableMap<String, ItemStack[]> map = ImmutableMap.<String, ItemStack[]>builder()
                .put("oak", new ItemStack[]{new ItemStack(Blocks.PLANKS), new ItemStack(Blocks.LOG), new ItemStack(Items.OAK_DOOR), new ItemStack(Blocks.WOODEN_SLAB)})
                .put("spruce", new ItemStack[]{new ItemStack(Blocks.PLANKS, 1, 1), new ItemStack(Blocks.LOG, 1, 1), new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Blocks.WOODEN_SLAB, 1, 1)})
                .put("birch", new ItemStack[]{new ItemStack(Blocks.PLANKS, 1, 2), new ItemStack(Blocks.LOG, 1, 2), new ItemStack(Items.BIRCH_DOOR), new ItemStack(Blocks.WOODEN_SLAB, 1, 2)})
                .put("jungle", new ItemStack[]{new ItemStack(Blocks.PLANKS, 1, 3), new ItemStack(Blocks.LOG, 1, 3), new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Blocks.WOODEN_SLAB, 1, 3)})
                .put("acacia", new ItemStack[]{new ItemStack(Blocks.PLANKS, 1, 4), new ItemStack(Blocks.LOG2), new ItemStack(Items.ACACIA_DOOR), new ItemStack(Blocks.WOODEN_SLAB, 1, 4)})
                .put("dark_oak", new ItemStack[]{new ItemStack(Blocks.PLANKS, 1, 5), new ItemStack(Blocks.LOG2, 1, 1), new ItemStack(Items.DARK_OAK_DOOR), new ItemStack(Blocks.WOODEN_SLAB, 1, 5)})
                .build();

        for (Map.Entry<String, ItemStack[]> entry : map.entrySet()) {
            final String name = entry.getKey();
            final String name_planks = name + "_planks";

            final ItemStack[] items = entry.getValue();
            final ItemStack planks = items[0];
            final ItemStack log = items[1];
            final ItemStack door = items[2];
            final ItemStack slab = items[3];

            ItemStack recipeOutput =  planks.copy();

            // log -> plank crafting
            if (ConfigHolder.recipes.nerfWoodCrafting) {
                ModHandler.removeRecipeByName(name_planks);
                recipeOutput.setCount(2);
                ModHandler.addShapelessRecipe(name_planks, recipeOutput, log.copy());
            }

            recipeOutput = recipeOutput.copy();
            recipeOutput.setCount(ConfigHolder.recipes.nerfWoodCrafting ? 4 : 6);
            ModHandler.addShapedRecipe(name_planks + "_saw", recipeOutput, "s", "L", 'L', log.copy());

            // log -> plank cutting
            CUTTER_RECIPES.recipeBuilder()
                    .inputs(log.copy())
                    .outputs(recipeOutput.copy())
                    .output(dust, Wood, 2)
                    .duration(200)
                    .EUt(VA[ULV])
                    .buildAndRegister();

            // plank -> door assembling
            recipeOutput = door.copy();
            recipeOutput.setCount(3);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copyAmount(6, planks))
                    .outputs(recipeOutput)
                    .circuitMeta(6)
                    .duration(600).EUt(4)
                    .buildAndRegister();

            // plank -> slab crafting
            recipeOutput = slab.copy();
            recipeOutput.setCount(2);
            ModHandler.addShapedRecipe(name + "_slab_saw", recipeOutput, "sS", 'S', planks.copy());

            // plank -> slab cutting
            CUTTER_RECIPES.recipeBuilder()
                    .inputs(planks.copy())
                    .outputs(recipeOutput.copy())
                    .duration(200).EUt(VA[ULV])
                    .buildAndRegister();

            // log -> charcoal furnace recipe
            if (ConfigHolder.recipes.harderCharcoalRecipe) {
                ItemStack outputStack = FurnaceRecipes.instance().getSmeltingResult(log);
                if (outputStack.getItem() == Items.COAL && outputStack.getItemDamage() == 1) {
                    ModHandler.removeFurnaceSmelting(log);
                }
            }
        }

        // GT wood special handling
        ModHandler.addShapelessRecipe("rubber_wood_planks", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK, ConfigHolder.recipes.nerfWoodCrafting ? 2 : 4), new ItemStack(MetaBlocks.RUBBER_LOG));
        ModHandler.addShapedRecipe("rubber_wood_planks_saw", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK, ConfigHolder.recipes.nerfWoodCrafting ? 4 : 6), "s", "L", 'L', new ItemStack(MetaBlocks.RUBBER_LOG));

        ModHandler.addShapedRecipe("treated_wood_planks", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK, 8), "PPP", "PBP", "PPP", 'P', "plankWood", 'B', FluidUtil.getFilledBucket(Creosote.getFluid(1000)));

        ModHandler.addShapedRecipe("treated_wood_stick", OreDictUnifier.get(OrePrefix.stick, TreatedWood, ConfigHolder.recipes.nerfWoodCrafting ? 2 : 4), "L", "L", 'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        if (ConfigHolder.recipes.nerfWoodCrafting) {
            ModHandler.addShapedRecipe("treated_wood_stick_saw", OreDictUnifier.get(OrePrefix.stick, TreatedWood, 4), "s", "L", 'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        }

        if (!ConfigHolder.recipes.harderCharcoalRecipe) {
            GameRegistry.addSmelting(MetaBlocks.RUBBER_LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);
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

        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(22)
                .input(block, Coal, 8)
                .fluidInputs(Steam.getFluid(4000))
                .output(block, Coke, 8)
                .fluidOutputs(CoalGas.getFluid(16000))
                .duration(1280).EUt(96)
                .buildAndRegister();

        // COAL TAR ============================================
        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(8)
                .inputs(new ItemStack(Items.COAL, 32, 1))
                .output(dustSmall, Ash, 2)
                .fluidOutputs(CoalTar.getFluid(1000))
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
