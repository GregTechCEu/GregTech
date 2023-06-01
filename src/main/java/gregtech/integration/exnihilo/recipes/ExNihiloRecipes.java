/*
    Copyright 2019, TheLimePixel, dan
    GregBlock Utilities

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gregtech.integration.exnihilo.recipes;

import exnihilocreatio.ModBlocks;
import exnihilocreatio.compatibility.jei.sieve.SieveRecipe;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.Siftable;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.exnihilo.ExNihiloModule;
import gregtech.loaders.recipe.MetaTileEntityLoader;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

import static gregtech.common.blocks.BlockSteamCasing.SteamCasingType.BRONZE_HULL;
import static gregtech.integration.exnihilo.ExNihiloModule.*;
import static gregtech.loaders.recipe.CraftingComponent.*;

public class ExNihiloRecipes {

    public static void registerHandlers() {
        ExNihiloModule.oreChunk.addProcessingHandler(PropertyKey.ORE, ExNihiloRecipes::processChunk);
        ExNihiloModule.oreEnderChunk.addProcessingHandler(PropertyKey.ORE, ExNihiloRecipes::processChunk);
        ExNihiloModule.oreNetherChunk.addProcessingHandler(PropertyKey.ORE, ExNihiloRecipes::processChunk);
        ExNihiloModule.oreSandyChunk.addProcessingHandler(PropertyKey.ORE, ExNihiloRecipes::processChunk);
    }

    private static void processChunk(OrePrefix orePrefix, Material material, OreProperty oreProperty) {
        Material smeltingMaterial = material;
        ItemStack smeltStack = ItemStack.EMPTY;
        if (oreProperty.getDirectSmeltResult() != null) {
            smeltingMaterial = oreProperty.getDirectSmeltResult();
        }

        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            smeltStack = OreDictUnifier.get(OrePrefix.ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            smeltStack = OreDictUnifier.get(OrePrefix.gem, smeltingMaterial);
        }
        if (!smeltStack.isEmpty() && !material.hasProperty(PropertyKey.BLAST)) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(orePrefix, material), smeltStack);
        }
    }

    public static void registerGTRecipes() {
        // Machine Recipes
        MetaTileEntityLoader.registerMachineRecipe(SIEVES, "CPC", "FMF", "OSO", 'M', HULL, 'C', CIRCUIT, 'O', CABLE, 'F', CONVEYOR, 'S', new ItemStack(ModBlocks.sieve), 'P', PISTON);
        ModHandler.addShapedRecipe("steam_sieve_bronze", STEAM_SIEVE_BRONZE.getStackForm(), "BPB", "BMB", "BSB", 'B', new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'M', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'S', new ItemStack(ModBlocks.sieve), 'P', Blocks.PISTON);
        ModHandler.addShapedRecipe("steam_sieve_steel", STEAM_SIEVE_STEEL.getStackForm(), "BPB", "WMW", "BBB", 'B', new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.WroughtIron), 'M', STEAM_SIEVE_BRONZE.getStackForm(), 'W', new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel));
    }

    // Has to be done in init phase because of ExNi registering outside of the Registry event
    public static void registerExNihiloRecipes() {
        // Mirror Ex Nihilo Sifter recipes to Sifter RecipeMap
        for (SieveRecipe recipe : ExNihiloRegistryManager.SIEVE_REGISTRY.getRecipeList()) {
            for (ItemStack stack : recipe.getSievables()) {
                if (SIEVE_RECIPES.findRecipe(4, Arrays.asList(stack, recipe.getMesh()), new ArrayList<>(), true) != null)
                    continue;
                SimpleRecipeBuilder builder = SIEVE_RECIPES.recipeBuilder().notConsumable(recipe.getMesh()).inputs(stack);

                for (Siftable siftable : ExNihiloRegistryManager.SIEVE_REGISTRY.getDrops(stack)) {
                    if (siftable.getMeshLevel() == recipe.getMesh().getMetadata())
                        if ((int) (siftable.getChance() * (float) Recipe.getMaxChancedValue()) >= Recipe.getMaxChancedValue()) {
                            builder.outputs(siftable.getDrop().getItemStack());
                        } else {
                            builder.chancedOutput(siftable.getDrop().getItemStack(), (int) (siftable.getChance() * (float) Recipe.getMaxChancedValue()), 500);
                        }
                }
                builder.buildAndRegister();
            }
        }
    }
}
