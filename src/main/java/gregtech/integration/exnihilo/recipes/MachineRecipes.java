package gregtech.integration.exnihilo.recipes;

import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.Mods;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

import exnihilocreatio.ModBlocks;
import exnihilocreatio.ModItems;
import exnihilocreatio.compatibility.jei.crucible.CrucibleRecipe;
import exnihilocreatio.compatibility.jei.sieve.SieveRecipe;
import exnihilocreatio.modules.AppliedEnergistics2;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.Siftable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.integration.exnihilo.ExNihiloModule.*;

public class MachineRecipes {

    public static void registerRecipes() {
        // Pebbles
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ModItems.pebbles, 4, 0))
                .output(Blocks.COBBLESTONE)
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ModItems.pebbles, 4, 1))
                .outputs(new ItemStack(Blocks.STONE, 1, 1))
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ModItems.pebbles, 4, 2))
                .outputs(new ItemStack(Blocks.STONE, 1, 3))
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ModItems.pebbles, 4, 3))
                .outputs(new ItemStack(Blocks.STONE, 1, 5))
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ExNihiloModule.pebbleItem, 4, 0))
                .outputs(OreDictUnifier.get(cobble, Basalt, 1))
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ExNihiloModule.pebbleItem, 4, 1))
                .outputs(OreDictUnifier.get(cobble, GraniteBlack, 1))
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ExNihiloModule.pebbleItem, 4, 2))
                .outputs(OreDictUnifier.get(cobble, Marble, 1))
                .EUt(4)
                .duration(40)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(ExNihiloModule.pebbleItem, 4, 3))
                .outputs(OreDictUnifier.get(cobble, GraniteRed, 1))
                .EUt(4)
                .duration(40)
                .buildAndRegister();

        // Crushed stones
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputs(Mods.AppliedEnergistics2.getItem("sky_stone_block"))
                    .output(AppliedEnergistics2.skystoneCrushed)
                    .EUt(16)
                    .duration(10)
                    .buildAndRegister();
        }
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(Blocks.SAND)
                .output(ModBlocks.dust)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE, 1, 1))
                .output(ModBlocks.crushedGranite)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE, 1, 3))
                .output(ModBlocks.crushedDiorite)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE, 1, 5))
                .output(ModBlocks.crushedAndesite)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(Blocks.NETHERRACK)
                .output(ModBlocks.netherrackCrushed)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        FORGE_HAMMER_RECIPES.recipeBuilder()
                .input(Blocks.END_STONE)
                .output(ModBlocks.endstoneCrushed)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
    }

    public static void mirrorExNihiloRecipes() {
        Map<ItemStack, List<ItemStack>> sieveRecipes = new Object2ObjectOpenHashMap<>();
        ArrayList<ItemStack> extractorRecipes = new ArrayList<>();

        // Mirror Ex Nihilo Sifter recipes to Sifter RecipeMap
        for (SieveRecipe recipe : ExNihiloRegistryManager.SIEVE_REGISTRY.getRecipeList()) {
            for (ItemStack stack : recipe.getSievables()) {
                SimpleRecipeBuilder builder = SIEVE_RECIPES.recipeBuilder().notConsumable(recipe.getMesh())
                        .inputs(stack);
                switch (recipe.getMesh().getMetadata()) {
                    case 1:
                        builder.EUt(8);
                        break;
                    case 2:
                        builder.EUt(16);
                        break;
                    case 3:
                        builder.EUt(32);
                        break;
                    case 4:
                        builder.EUt(64);
                        break;
                }
                if (sieveRecipes.containsKey(stack) && sieveRecipes.get(stack).contains(recipe.getMesh()))
                    continue;
                for (Siftable siftable : ExNihiloRegistryManager.SIEVE_REGISTRY.getDrops(stack)) {
                    if (siftable.getDrop() == null) continue;
                    if (siftable.getMeshLevel() == recipe.getMesh().getMetadata()) {
                        int maxChance = ChancedOutputLogic.getMaxChancedValue();
                        if ((int) (siftable.getChance() * (float) maxChance) >= maxChance) {
                            builder.outputs(siftable.getDrop().getItemStack());
                        } else {
                            builder.chancedOutput(siftable.getDrop().getItemStack(),
                                    (int) (siftable.getChance() * (float) maxChance), 500);
                        }
                    }
                }
                if (sieveRecipes.keySet().stream().noneMatch(stack::isItemEqual)) {
                    sieveRecipes.put(stack, new ArrayList<>());
                }
                sieveRecipes.computeIfAbsent(stack, key -> new ArrayList<>()).add(recipe.getMesh());
                builder.buildAndRegister();
            }
        }

        // Mirror Crucible recipes to Extractor RecipeMap if enabled in config
        if (ExNihiloConfig.crucibleExtractorRecipes) {
            for (CrucibleRecipe recipe : ExNihiloRegistryManager.CRUCIBLE_STONE_REGISTRY.getRecipeList()) {
                if (FluidUtil.getFluidContained(recipe.getFluid()) != null) {
                    for (List<ItemStack> listStack : recipe.getInputs()) {
                        for (ItemStack stack : listStack) {
                            if (extractorRecipes.stream().anyMatch(stack::isItemEqual)) {
                                continue;
                            }
                            extractorRecipes.add(stack);
                            EXTRACTOR_RECIPES.recipeBuilder()
                                    .inputs(stack)
                                    .fluidOutputs(FluidUtil.getFluidContained(recipe.getFluid()))
                                    .duration(100)
                                    .buildAndRegister();
                        }
                    }
                }
            }
        }
    }
}
