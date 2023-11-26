package gregtech.api.recipes;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RecyclingHandler {

    public static @Nullable ItemMaterialInfo getRecyclingIngredients(int outputCount, @NotNull Object... recipe) {
        Char2IntOpenHashMap inputCountMap = new Char2IntOpenHashMap();
        Object2LongMap<Material> materialStacksExploded = new Object2LongOpenHashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String s) {
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                int count = inputCountMap.getOrDefault(c, 0);
                inputCountMap.put(c, count + 1);
            }
            itr++;
        }

        char lastChar = ' ';
        for (int i = itr; i < recipe.length; i++) {
            Object ingredient = recipe[i];

            // Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            // Should never happen if recipe is formatted correctly
            // In the case that it isn't, this error should be handled
            // by an earlier method call parsing the recipe.
            if (lastChar == ' ') return null;

            ItemStack stack;
            if (ingredient instanceof MetaItem.MetaValueItem) {
                stack = ((MetaItem<?>.MetaValueItem) ingredient).getStackForm();
            } else if (ingredient instanceof UnificationEntry) {
                stack = OreDictUnifier.get((UnificationEntry) ingredient);
            } else if (ingredient instanceof ItemStack) {
                stack = (ItemStack) ingredient;
            } else if (ingredient instanceof Item) {
                stack = new ItemStack((Item) ingredient, 1);
            } else if (ingredient instanceof Block) {
                stack = new ItemStack((Block) ingredient, 1);
            } else if (ingredient instanceof String) {
                stack = OreDictUnifier.get((String) ingredient);
            } else continue; // throw out bad entries

            addItemStackToMaterialStacks(stack, materialStacksExploded, inputCountMap.get(lastChar));
        }

        return new ItemMaterialInfo(materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue() / outputCount))
                .sorted(Comparator.comparingLong(m -> -m.amount))
                .collect(Collectors.toList()));
    }

    public static @Nullable ItemMaterialInfo getRecyclingIngredients(List<GTRecipeInput> inputs, int outputCount) {
        Object2LongMap<Material> materialStacksExploded = new Object2LongOpenHashMap<>();
        for (GTRecipeInput input : inputs) {
            if (input == null || input.isNonConsumable()) continue;
            ItemStack[] inputStacks = input.getInputStacks();
            if (inputStacks == null || inputStacks.length == 0) continue;
            ItemStack inputStack = inputStacks[0];
            addItemStackToMaterialStacks(inputStack, materialStacksExploded, inputStack.getCount());
        }

        return new ItemMaterialInfo(materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue() / outputCount))
                .sorted(Comparator.comparingLong(m -> -m.amount))
                .collect(Collectors.toList()));
    }

    private static void addItemStackToMaterialStacks(@NotNull ItemStack itemStack,
                                                     @NotNull Object2LongMap<Material> materialStacksExploded,
                                                     int inputCount) {
        // First try to get ItemMaterialInfo
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(itemStack);
        if (info != null) {
            for (MaterialStack ms : info.getMaterials()) {
                if (!(ms.material instanceof MarkerMaterial)) {
                    addMaterialStack(materialStacksExploded, inputCount, ms);
                }
            }
            return;
        }

        // Then try to get a single Material (UnificationEntry needs this, for example)
        MaterialStack materialStack = OreDictUnifier.getMaterial(itemStack);
        if (materialStack != null && !(materialStack.material instanceof MarkerMaterial)) {
            addMaterialStack(materialStacksExploded, inputCount, materialStack);
        }

        // Gather any secondary materials if this item has an OrePrefix
        OrePrefix prefix = OreDictUnifier.getPrefix(itemStack);
        if (prefix != null && !prefix.secondaryMaterials.isEmpty()) {
            for (MaterialStack ms : prefix.secondaryMaterials) {
                addMaterialStack(materialStacksExploded, inputCount, ms);
            }
        }
    }

    /**
     * Adds a MaterialStack to a map of {@code <Material, Quantity>}
     *
     * @param materialStacksExploded the map to add to
     * @param inputCount             the number of items in the stack
     * @param ms                     the stack to add
     */
    private static void addMaterialStack(@NotNull Object2LongMap<Material> materialStacksExploded,
                                         int inputCount, @NotNull MaterialStack ms) {
        long amount = materialStacksExploded.getOrDefault(ms.material, 0L);
        materialStacksExploded.put(ms.material, (ms.amount * inputCount) + amount);
    }
}
