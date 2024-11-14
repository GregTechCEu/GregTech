package gregtech.common.command;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.ingredients.OreItemIngredient;
import gregtech.api.recipes.ingredients.old.IntCircuitIngredient;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;
import gregtech.api.recipes.output.FluidOutputProvider;
import gregtech.api.recipes.output.ItemOutputProvider;
import gregtech.api.recipes.output.StandardFluidOutput;
import gregtech.api.recipes.output.StandardItemOutput;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
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
import net.minecraftforge.oredict.OreDictionary;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class CommandRecipeCheck extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "recipecheck";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "gregtech.command.util.recipecheck.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String[] args) {
        sender.sendMessage(new TextComponentTranslation("gregtech.command.recipecheck.begin"));

        Object2ObjectOpenHashMap<RecipeMap<?>, Object2ObjectOpenHashMap<Recipe, ReferenceOpenHashSet<Recipe>>> mismatchedRecipes = new Object2ObjectOpenHashMap<>();
        Object2ObjectOpenHashMap<RecipeMap<?>, Set<Recipe>> emptyInputRecipes = new Object2ObjectOpenHashMap<>();
        IntSet emptyOreDicts = new IntOpenHashSet();

        GTLog.logger.info("[Recipe Checker] Starting recipe issue check...");
        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            Object2ObjectOpenHashMap<Recipe, ReferenceOpenHashSet<Recipe>> equivalency = new Object2ObjectOpenHashMap<>(
                    recipeMap.getRecipeList().size());
            mismatchedRecipes.put(recipeMap, equivalency);
            emptyInputRecipes.put(recipeMap, new ObjectOpenHashSet<>());
            GTLog.logger.info("Checking Recipe Map: {}", recipeMap.unlocalizedName);
            for (Recipe currentRecipe : recipeMap.getRecipeList()) {
                // check for any empty or null inputs
                for (GTItemIngredient input : currentRecipe.getItemIngredients()) {
                    if (input == null) {
                        emptyInputRecipes.get(recipeMap).add(currentRecipe);
                        continue;
                    }
                    int count = 0;
                    for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
                        count += input.getMatchingStacksWithinContext(context).size();
                    }
                    if (count == 0) {
                        emptyInputRecipes.get(recipeMap).add(currentRecipe);
                        if (input instanceof OreItemIngredient i) {
                            emptyOreDicts.add(i.getOreID());
                        }
                    }
                }
            }
            var iter = equivalency.object2ObjectEntrySet().fastIterator();
            while (iter.hasNext()) {
                // remove the recipes that did not collide.
                if (iter.next().getValue().isEmpty()) iter.remove();
            }
            if (mismatchedRecipes.get(recipeMap).isEmpty()) {
                GTLog.logger.info("No mismatched recipes found for recipe map: {}", recipeMap.unlocalizedName);
                mismatchedRecipes.remove(recipeMap);
            } else {
                GTLog.logger.error("Mismatched recipes found for recipe map: {}", recipeMap.unlocalizedName);
            }
            if (emptyInputRecipes.get(recipeMap).isEmpty()) {
                emptyInputRecipes.remove(recipeMap);
            } else {
                GTLog.logger.error("Recipes with empty inputs found in recipe map: {}", recipeMap.unlocalizedName);
            }
        }

        GTLog.logger.info("[Recipe Checker] Completed recipe check!");
        int count = 0;
        if (mismatchedRecipes.size() == 0) {
            GTLog.logger.info("No recipe conflicts found in all recipe maps!");
        } else {
            count = (int) mismatchedRecipes.values().stream()
                    .mapToLong(s -> s.values().stream().mapToLong(Set::size).sum()).sum();
            GTLog.logger.info("[Recipe Checker] Found {} potential conflicts", count);
            for (var recipeMap : mismatchedRecipes.entrySet()) {
                GTLog.logger.error(
                        "\n[In Recipe map] : \"{}\"", recipeMap.getKey().unlocalizedName);
                for (var reciper : mismatchedRecipes.get(recipeMap.getKey()).entrySet()) {
                    StringBuilder conflictingRecipes = new StringBuilder();
                    conflictingRecipes.append("\n[Tried matching]: ").append(prettyPrintRecipe(reciper.getKey()));
                    for (Recipe c : reciper.getValue()) {
                        conflictingRecipes.append("\n[Also Found]: ").append(prettyPrintRecipe(c));
                    }
                    GTLog.logger.error(conflictingRecipes.toString());
                }
            }
        }

        int emptyCount = 0;
        if (emptyInputRecipes.size() != 0) {
            emptyCount = (int) emptyInputRecipes.values().stream().mapToLong(Set::size).sum();
            GTLog.logger.info("[Recipe Checker] Found {} recipes with empty inputs", emptyCount);
            for (Map.Entry<RecipeMap<?>, Set<Recipe>> recipeMap : emptyInputRecipes.entrySet()) {
                GTLog.logger.error("\n[In Recipe map] : \"{}\"", recipeMap.getKey().unlocalizedName);
                for (Recipe recipe : recipeMap.getValue()) {
                    GTLog.logger.error("\n" + prettyPrintRecipe(recipe));
                }
            }

            GTLog.logger.info("[Recipe Checker] Found {} empty oredicts", emptyOreDicts.size());
            if (emptyOreDicts.size() > 0) {
                StringBuilder oredicts = new StringBuilder();
                for (int dictID : emptyOreDicts) {
                    oredicts.append("\n").append(OreDictionary.getOreName(dictID));
                }
                GTLog.logger.error(oredicts.toString());
            }
        }

        if (mismatchedRecipes.size() == 0) {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.recipecheck.end_no_conflicts")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.recipecheck.end", count));
        }
        if (emptyInputRecipes.size() != 0) {
            sender.sendMessage(new TextComponentTranslation("gregtech.command.recipecheck.end_empty_inputs", emptyCount,
                    emptyOreDicts.size()));
        }
    }

    public static String prettyPrintRecipe(Recipe recipe) {
        if (recipe == null) {
            return "null (Is something else going wrong?)\n";
        }
        StringBuilder output = new StringBuilder();
        output.append("EU/t: ")
                .append(recipe.getVoltage())
                .append(", Duration: ")
                .append(recipe.getDuration());
        if (recipe.isHidden()) {
            output.append(", hidden");
        }
        output.append("\n");

        if (!recipe.getItemIngredients().isEmpty()) {
            output.append("Item inputs:\n");
            for (GTItemIngredient ingredient : recipe.getItemIngredients()) {
                output.append("    ")
                        .append(prettyPrintRecipeInput(ingredient))
                        .append("\n");
            }
        }

        if (!recipe.getFluidIngredients().isEmpty()) {
            output.append("Fluid inputs:\n");
            for (GTFluidIngredient fluid : recipe.getFluidIngredients()) {
                output.append("    ")
                        .append(prettyPrintRecipeInput(fluid))
                        .append("\n");
            }
        }

        ItemOutputProvider itemProvider = recipe.getItemOutputProvider();

        if (itemProvider.getMaximumOutputs(1) > 0) {
            if (itemProvider instanceof StandardItemOutput standard) {
                if (!standard.getOutputs().isEmpty()) {
                    output.append("Item outputs:\n");
                    for (ItemStack stack : standard.getOutputs()) {
                        output.append("    ")
                                .append(prettyPrintItemStack(stack))
                                .append("\n");
                    }
                }

                if (!standard.getChancedEntries().isEmpty()) {
                    output.append("Item chanced outputs:\n");
                    for (ChancedItemOutput chanceEntry : standard.getChancedEntries()) {
                        output.append("    ")
                                .append(prettyPrintItemStack(chanceEntry.getIngredient()))
                                .append(" (Chance: ")
                                .append(chanceEntry.getChance())
                                .append(", Boost: ")
                                .append(chanceEntry.getChanceBoost())
                                .append(")\n");
                    }
                }
            } else {
                output.append("Custom Item Output Provider.\n");
            }
        }

        FluidOutputProvider fluidProvider = recipe.getFluidOutputProvider();

        if (fluidProvider.getMaximumOutputs(1) > 0) {
            if (fluidProvider instanceof StandardFluidOutput standard) {
                if (!standard.getOutputs().isEmpty()) {
                    output.append("Fluid outputs:\n");
                    for (FluidStack fluid : standard.getOutputs()) {
                        output.append("    ")
                                .append(fluid.getUnlocalizedName())
                                .append(" * ")
                                .append(fluid.amount)
                                .append("\n");
                    }
                }

                if (!standard.getChancedEntries().isEmpty()) {
                    output.append("Fluid chanced outputs:\n");
                    for (ChancedFluidOutput chanceEntry : standard.getChancedEntries()) {
                        output.append("    ")
                                .append(chanceEntry.getIngredient().getUnlocalizedName())
                                .append(" (Chance: ")
                                .append(chanceEntry.getChance())
                                .append(", Boost: ")
                                .append(chanceEntry.getChanceBoost())
                                .append(")\n");
                    }
                }
            } else {
                output.append("Custom Fluid Output Provider.\n");
            }
        }

        return output.toString();
    }

    public static @NotNull String prettyPrintRecipeInput(GTItemIngredient recipeInput) {
        StringBuilder output = new StringBuilder();
        if (recipeInput instanceof OreItemIngredient ore) {
            output.append("(OreDict: ")
                    .append("\"")
                    .append(OreDictionary.getOreName(ore.getOreID()))
                    .append("\")");
        }
        output.append(" { ");

        boolean one = false;
        for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
            for (ItemStack stack : recipeInput.getMatchingStacksWithinContext(context)) {
                one = true;
                output.append(" ")
                        .append(prettyPrintItemStack(stack))
                        .append(",");
            }
        }
        if (one) {
            output.delete(output.lastIndexOf(","), output.length());
        }
        output.append(" } * ")
                .append(recipeInput.getRequiredCount());
        return output.toString();
    }

    public static @NotNull String prettyPrintRecipeInput(GTFluidIngredient recipeInput) {
        StringBuilder output = new StringBuilder();
        output.append(" { ");

        boolean one = false;
        for (FluidStackMatchingContext context : FluidStackMatchingContext.VALUES) {
            for (FluidStack stack : recipeInput.getMatchingStacksWithinContext(context)) {
                one = true;
                output.append(" ")
                        .append(stack.getUnlocalizedName())
                        .append(",");
            }
        }
        if (one) {
            output.delete(output.lastIndexOf(","), output.length());
        }
        output.append(" } * ")
                .append(recipeInput.getRequiredCount());
        return output.toString();
    }

    public static String prettyPrintItemStack(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem<?>metaItem) {
            MetaValueItem metaValueItem = metaItem.getItem(stack);
            if (metaValueItem == null) {
                if (metaItem instanceof MetaPrefixItem metaPrefixItem) {
                    Material material = metaPrefixItem.getMaterial(stack);
                    OrePrefix orePrefix = metaPrefixItem.getOrePrefix();
                    return "(MetaItem) OrePrefix: " + orePrefix.name + ", Material: " + material + " * " +
                            stack.getCount();
                }
            } else {
                if (MetaItems.INTEGRATED_CIRCUIT.isItemEqual(stack)) {
                    return "Config circuit #" + IntCircuitIngredient.getCircuitConfiguration(stack);
                }
                return "(MetaItem) " + metaValueItem.unlocalizedName + " * " + stack.getCount();
            }
        } else if (stack.getItem() instanceof MachineItemBlock) {
            MetaTileEntity mte = GTUtility.getMetaTileEntity(stack);
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
                id = "block" + ((BlockCompressed) block).getGtMaterial(stack).toCamelCaseString();
            } else if (block instanceof BlockFrame) {
                id = "frame" + ((BlockFrame) block).getGtMaterial(stack).toCamelCaseString();
            } else if (block instanceof BlockMaterialPipe<?, ?, ?>blockMaterialPipe) {
                id = blockMaterialPipe.getPrefix().name + blockMaterialPipe.getItemMaterial(stack).toCamelCaseString();
            }

            if (id != null) {
                return "(MetaBlock) " + id + " * " + stack.getCount();
            }
        }
        // noinspection ConstantConditions
        return stack.getItem().getRegistryName().toString() + " * " + stack.getCount() + " (Meta " +
                stack.getItemDamage() + ")";
    }
}
