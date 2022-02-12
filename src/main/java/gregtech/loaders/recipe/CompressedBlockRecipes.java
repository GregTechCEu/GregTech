package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.ModHandler;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

import static gregtech.api.recipes.RecipeMaps.*;

public class CompressedBlockRecipes {

    public static void postInit() {
        processCompressedBlocks();
    }

    private static void processCompressedBlocks() {

        List<ResourceLocation> recipesToRemove = new ArrayList<>();

        if(ConfigHolder.recipes.disableManualCompressionForOtherMaterials) {
            for (IRecipe recipe : CraftingManager.REGISTRY) {
                if (recipe.getIngredients().size() == 9) {
                    // There is a pre-existing config that deals with GT materials and MC blocks
                    if (!(recipe.getRegistryName().getNamespace().equals(GTValues.MODID) || recipe.getRegistryName().getNamespace().equals("minecraft"))) {
                        if (!recipe.getRecipeOutput().isEmpty() && !(Block.getBlockFromItem(recipe.getRecipeOutput().getItem()) == Blocks.AIR)) {

                            Ingredient[] inputStack = new Ingredient[9];
                            // Gather all the ingredients into an array
                            for (int i = 0; i < 9; i++) {
                                inputStack[i] = recipe.getIngredients().get(i);
                            }

                            // Check if the ingredients are all the same material
                            boolean allSame = Arrays.stream(inputStack).distinct().count() == 1;

                            if (allSame) {
                                // Add the recipe to be removed
                                recipesToRemove.add(recipe.getRegistryName());

                                // The ItemStack input for the recipe, in case there are no matching stacks
                                ItemStack inputItemStack = ItemStack.EMPTY;

                                // The Ore Dict name input for the recipe, in case there are matching stacks
                                String finalOreDictName = "";

                                List<int[]> oreDictNames = new ArrayList<>();
                                int[] oreIds;
                                // Gather all the Ore Dict names for all the matching ingredients
                                // If there are other matching items for the input, most likely because of oredict
                                if (recipe.getIngredients().get(0).getMatchingStacks().length > 1) {
                                    // Get all matching items
                                    for (ItemStack input : recipe.getIngredients().get(0).getMatchingStacks()) {
                                        // Get all Ore Dicts the item has
                                        oreIds = OreDictionary.getOreIDs(input);
                                        // If there is only 1 Ore Dict on the item, exit quickly
                                        if (oreIds.length == 1) {
                                            finalOreDictName = OreDictionary.getOreName(oreIds[0]);
                                            break;
                                        }
                                        oreDictNames.add(oreIds);
                                    }

                                    // If we have not found an item in all matching stacks with only 1 ore dict, attempt to find
                                    // the common oredict between all items
                                    if (finalOreDictName.isEmpty()) {
                                        List<Integer> matchingIds = new ArrayList<>();
                                        for (int[] ids : oreDictNames) {
                                            // On first run, populate the list with all ore dict ids
                                            if (matchingIds.isEmpty()) {
                                                Arrays.stream(ids).forEach(matchingIds::add);
                                            } else {
                                                List<Integer> realMatchingIDs = new ArrayList<>();
                                                for (int id : ids) {
                                                    // If the list of matching ore dict ids contains the current id, add it to the new list
                                                    if (matchingIds.contains(id)) {
                                                        realMatchingIDs.add(id);
                                                    }
                                                }
                                                // Replace the list of matching ids with the updated list of matching ids
                                                if (!realMatchingIDs.isEmpty()) {
                                                    matchingIds = realMatchingIDs;
                                                }
                                            }
                                        }

                                        // At this point, always take the first
                                        finalOreDictName = OreDictionary.getOreName(matchingIds.get(0));
                                    }
                                } else {
                                    // If there are no matching stacks, just take the item
                                    inputItemStack = recipe.getIngredients().get(0).getMatchingStacks()[0];
                                }


                                // Register Compressor recipes for the removed crafting table compression recipes
                                if (!inputItemStack.isEmpty()) {
                                    COMPRESSOR_RECIPES.recipeBuilder()
                                            .inputs(CountableIngredient.from(inputItemStack, recipe.getIngredients().size()))
                                            .outputs(recipe.getRecipeOutput())
                                            .duration(400)
                                            .EUt(2)
                                            .buildAndRegister();
                                } else {
                                    COMPRESSOR_RECIPES.recipeBuilder()
                                            .inputs(CountableIngredient.from(finalOreDictName, recipe.getIngredients().size()))
                                            .outputs(recipe.getRecipeOutput())
                                            .duration(400)
                                            .EUt(2)
                                            .buildAndRegister();

                                }
                            }
                        }
                    }
                }
            }
        }


        for (ResourceLocation r : recipesToRemove) {
            ModHandler.removeRecipeByName(r);
        }
    }
}
