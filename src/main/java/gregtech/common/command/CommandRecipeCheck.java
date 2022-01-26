package gregtech.common.command;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandRecipeCheck extends CommandBase {

    @Nonnull
    @Override
    public String getName() {
        return "recipecheck";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.util.recipecheck.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        sender.sendMessage(new TextComponentTranslation("gregtech.command.util.recipecheck.begin"));

        List<MismatchEntry> mismatchedRecipes = new ArrayList<>();

        GTLog.logger.info("[Recipe Checker] Starting recipe conflict check...");
        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            for (Recipe recipe : recipeMap.getRecipeList()) {
                MismatchEntry checkResult = checkRecipe(
                        recipe,
                        recipeMap,
                        recipe.getFluidInputs()
                                // multiply volume of fluids by 10000 to detect conflicts only occurring if batching the recipe
                                .stream().map(stack -> new FluidStack(stack, stack.amount * 10000))
                                .collect(Collectors.toList()),
                        recipe.getInputs(), 0);
                if (checkResult != null) {
                    mismatchedRecipes.add(checkResult);
                }
            }
        }

        GTLog.logger.info("[Recipe Checker] Completed recipe check!");
        if (mismatchedRecipes.size() == 0) {
            GTLog.logger.info("No recipe conflicts found!");
        } else {
            GTLog.logger.info("[Recipe Checker] Found " + mismatchedRecipes.size() + " potential conflicts");
            for (MismatchEntry mismatch : mismatchedRecipes) {
                GTLog.logger.error(
                        "\nIn map " + mismatch.recipeMap.getUnlocalizedName() +
                                "\nTried: " + prettyPrintRecipe(mismatch.attempted) +
                                "\nFound: " + prettyPrintRecipe(mismatch.found)
                );
            }
        }

        if (mismatchedRecipes.size() == 0) {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.util.recipecheck.end_no_conflicts")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.util.recipecheck.end", mismatchedRecipes.size()));
        }
    }

    /**
     * Recursively check all combinations of item inputs until one is mismatched or there are no more
     */
    private MismatchEntry checkRecipe(Recipe recipe, RecipeMap<?> recipeMap, List<FluidStack> fluidInputs, List<CountableIngredient> itemInputs, int startIndex) {
        // if we have not yet bottomed out the input list
        if (startIndex < itemInputs.size()) {
            CountableIngredient ingredient = itemInputs.get(startIndex);
            ItemStack[] matchingStacks = ingredient.getIngredient().getMatchingStacks();
            if (matchingStacks.length > 1) {
                // check each possible input for this level
                for (ItemStack stack : matchingStacks) {
                    // shallow copy the input list to avoid editing it
                    List<CountableIngredient> reducedIngredients = new ArrayList<>(itemInputs);
                    // reduce the current level to only the current ItemStack
                    reducedIngredients.set(startIndex, CountableIngredient.from(stack, ingredient.getCount()));
                    // go one level deeper into the input list
                    MismatchEntry checkResult = checkRecipe(recipe, recipeMap, fluidInputs, reducedIngredients, startIndex + 1);
                    // only trigger the return chain if we found a mismatch, we want to continue the loop otherwise
                    if (checkResult != null){
                        return checkResult;
                    }
                }
            }
            else {
                // go one level deeper into the input list
                return checkRecipe(recipe, recipeMap, fluidInputs, itemInputs, startIndex + 1);
            }
        }
        else {
            // when at the bottom of the input list, actually check the recipe
            Recipe foundRecipe = recipeMap.findRecipe(Long.MAX_VALUE, recipe.getInputs().stream().map(ingredient -> {
                        // transform the CountableIngredient into a List<ItemStack>
                        ItemStack[] stacks = ingredient.getIngredient().getMatchingStacks();
                        if (stacks.length > 0) {
                            ItemStack outStack = stacks[0].copy();
                            // non-consumed inputs have a stack size of 0, correct that to 1
                            // multiply amount of items by 100 to detect conflicts only occurring if batching the recipe
                            outStack.setCount(Math.max(1, ingredient.getCount()) * 100);
                            return outStack;
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(Collectors.toList()),
                    fluidInputs, Integer.MAX_VALUE);
            // checks whether the same object is returned
            if (foundRecipe != recipe) {
                return new MismatchEntry(recipe, foundRecipe, recipeMap);
            }
            return null;
        }
        // only hit when the stack reduction loop finishes without finding a conflict
        return null;
    }

    public String prettyPrintRecipe(Recipe recipe) {
        if (recipe == null) {
            return "null (Is something else going wrong?)\n";
        }
        StringBuilder output = new StringBuilder();
        output.append("EU/t: ")
                .append(recipe.getEUt())
                .append(", Duration: ")
                .append(recipe.getDuration());
        if (recipe.isHidden()) {
            output.append(", hidden");
        }
        output.append("\n");

        if (recipe.getInputs().size() > 0) {
            output.append("Item inputs:\n");
            for (CountableIngredient ingredient : recipe.getInputs()) {
                output.append("    ")
                        .append(prettyPrintCountableIngredient(ingredient))
                        .append("\n");
            }
        }

        if (recipe.getFluidInputs().size() > 0) {
            output.append("Fluid inputs:\n");
            for (FluidStack fluid : recipe.getFluidInputs()) {
                output.append("    ")
                        .append(fluid.getUnlocalizedName())
                        .append(" * ")
                        .append(fluid.amount)
                        .append("\n");
            }
        }

        if (recipe.getOutputs().size() > 0) {
            output.append("Item outputs:\n");
            for (ItemStack stack : recipe.getOutputs()) {
                output.append("    ")
                        .append(prettyPrintItemStack(stack))
                        .append("\n");
            }
        }

        if (recipe.getChancedOutputs().size() > 0) {
            output.append("Item chanced outputs:\n");
            for (Recipe.ChanceEntry chanceEntry : recipe.getChancedOutputs()) {
                output.append("    ")
                        .append(prettyPrintItemStack(chanceEntry.getItemStack()))
                        .append(" (Chance: ")
                        .append(chanceEntry.getChance())
                        .append(", Boost: ")
                        .append(chanceEntry.getBoostPerTier())
                        .append(")\n");
            }
        }

        if (recipe.getFluidOutputs().size() > 0) {
            output.append("Fluid outputs:\n");
            for (FluidStack fluid : recipe.getFluidOutputs()) {
                output.append("    ")
                        .append(fluid.getUnlocalizedName())
                        .append(" * ")
                        .append(fluid.amount)
                        .append("\n");
            }
        }

        return output.toString();
    }

    public String prettyPrintCountableIngredient(CountableIngredient countableIngredient) {
        StringBuilder output = new StringBuilder();
        if (countableIngredient.getIngredient() instanceof OreIngredient) {
            output.append("(OreDict) ");
        }
        output.append("{");
        ItemStack[] matchingStacks = countableIngredient.getIngredient().getMatchingStacks();
        for (ItemStack stack : matchingStacks) {
            output.append(" ")
                    .append(prettyPrintItemStack(stack))
                    .append(",");
        }
        if (matchingStacks.length > 0) {
            output.delete(output.lastIndexOf(","), output.length());
        }
        output.append(" } * ")
                .append(countableIngredient.getCount());
        return output.toString();
    }

    public String prettyPrintItemStack(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) stack.getItem();
            MetaValueItem metaValueItem = metaItem.getItem(stack);
            if (metaValueItem == null) {
                if (metaItem instanceof MetaPrefixItem) {
                    Material material = ((MetaPrefixItem) metaItem).getMaterial(stack);
                    OrePrefix orePrefix = ((MetaPrefixItem) metaItem).getOrePrefix();
                    return "(MetaItem) OrePrefix: " + orePrefix.name + ", Material: " + material + " * " + stack.getCount();
                }
            } else {
                if (MetaItems.INTEGRATED_CIRCUIT.isItemEqual(stack)) {
                    return "Config circuit #" + IntCircuitIngredient.getCircuitConfiguration(stack);
                }
                return "(MetaItem) " + metaValueItem.unlocalizedName + " * " + stack.getCount();
            }
        } else if (stack.getItem() instanceof MachineItemBlock) {
                MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(stack);
                if (mte != null) {
                    String id = mte.metaTileEntityId.toString();
                    if (mte.metaTileEntityId.getNamespace().equals("gregtech"))
                        id = mte.metaTileEntityId.getPath();
                    return "(MetaTileEntity) " + id + " * " + stack.getCount();
                }
            } else {
                Block block = Block.getBlockFromItem(stack.getItem());
                String id = null;
                if (block instanceof BlockCompressed) {
                    id = "block" + ((BlockCompressed) block).getGtMaterial(stack.getMetadata()).toCamelCaseString();
                } else if (block instanceof BlockFrame) {
                    id = "frame" + ((BlockFrame) block).getGtMaterial(stack.getMetadata()).toCamelCaseString();
                } else if (block instanceof BlockMaterialPipe) {
                    id = ((BlockMaterialPipe<?, ?, ?>) block).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>) block).getItemMaterial(stack).toCamelCaseString();
                }

                if (id != null) {
                    return "(MetaBlock) " + id + " * " + stack.getCount();
                }
            }
        //noinspection ConstantConditions
        return stack.getItem().getRegistryName().toString() + " * " + stack.getCount() + " (Meta " + stack.getItemDamage() + ")";
    }

    private static class MismatchEntry {
        public Recipe attempted;
        public Recipe found;
        public RecipeMap<?> recipeMap;

        public MismatchEntry(Recipe attempted, Recipe found, RecipeMap<?> recipeMap) {
            this.attempted = attempted;
            this.found = found;
            this.recipeMap = recipeMap;
        }
    }
}
