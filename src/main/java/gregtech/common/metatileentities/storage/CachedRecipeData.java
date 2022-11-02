package gregtech.common.metatileentities.storage;

import gregtech.api.recipes.KeySharedStack;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.ShapedOreEnergyTransferRecipe;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.itemsource.ItemSources;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CachedRecipeData {
    private final ItemSources itemSources;
    private IRecipe recipe;
    private final Map<ItemStackKey, Integer> requiredItems = new HashMap<>();
    private final Map<Integer, Map<ItemStackKey, Boolean>> replaceAttemptMap = new Int2ObjectArrayMap<>();
    private final InventoryCrafting inventory;

    public CachedRecipeData(ItemSources sourceList, IRecipe recipe, InventoryCrafting inventoryCrafting) {
        this.itemSources = sourceList;
        this.recipe = recipe;
        this.inventory = inventoryCrafting;
    }

    public short attemptMatchRecipe() {
        short itemsFound = 0;
        this.requiredItems.clear();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (getIngredientEquivalent(i))
                itemsFound += 1 << i; //ingredient was found, and indicate in the short of this fact
        }
        if (itemsFound != CraftingRecipeLogic.ALL_INGREDIENTS_PRESENT) {
            requiredItems.clear();
        }
        return itemsFound;
    }

    protected boolean consumeRecipeItems() {
        boolean gathered = true;
        HashMap<ItemStackKey, Integer> gatheredItems = new HashMap<>();
        if (requiredItems.isEmpty()) {
            return false;
        }
        for (Entry<ItemStackKey, Integer> entry : requiredItems.entrySet()) {
            ItemStackKey itemStackKey = entry.getKey();
            int requestedAmount = entry.getValue();
            int extractedAmount = itemSources.extractItem(itemStackKey, requestedAmount, false);
            if (extractedAmount != requestedAmount) {
                gatheredItems.put(itemStackKey, extractedAmount);
                gathered = false;
                break;
            } else {
                gatheredItems.put(itemStackKey, requestedAmount);
            }
        }
        if (!gathered) {
            for (Entry<ItemStackKey, Integer> entry : gatheredItems.entrySet()) {
                itemSources.insertItem(entry.getKey(), entry.getValue(), false, IItemList.InsertMode.HIGHEST_PRIORITY);
            }
        }
        return gathered;
    }

    public boolean getIngredientEquivalent(int slot) {
        ItemStack currentStack = inventory.getStackInSlot(slot);
        if (currentStack.isEmpty()) {
            return true; //stack is empty, nothing to return
        }

        if (simulateExtractItem(KeySharedStack.getRegisteredStack(currentStack))) {
            return true;
        }

        ItemStack previousStack = recipe.getCraftingResult(inventory);

        Map<ItemStackKey, Boolean> map = replaceAttemptMap.computeIfAbsent(slot, (m) -> new Object2BooleanOpenHashMap<>());

        //iterate stored items to find equivalent
        for (ItemStackKey itemStackKey : itemSources.getStoredItems()) {
            if (map.containsKey(itemStackKey)) {
                if (!map.get(itemStackKey)) {
                    continue;
                } else {
                    return true;
                }
            }

            ItemStack itemStack = itemStackKey.getItemStack();

            boolean matched = false;
            //Matching shapeless recipes actually is very bad for performance, as it checks the entire
            //recipe ingredients recursively, so we fail early here if none of the recipes ingredients can
            //take the stack
            for (Ingredient in : recipe.getIngredients()) {
                if (in.apply(itemStack)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                map.put(itemStackKey, false);
                continue;
            }

            //update item in slot, and check that recipe matches and output item is equal to the expected one
            inventory.setInventorySlotContents(slot, itemStack);
            if (recipe.matches(inventory, itemSources.getWorld()) &&
                    (ItemStack.areItemStacksEqual(recipe.getCraftingResult(inventory), previousStack) || recipe instanceof ShapedOreEnergyTransferRecipe)) {
                map.put(itemStackKey, true);
                //ingredient matched, attempt to extract it and return if successful
                if (simulateExtractItem(itemStackKey)) {
                    return true;
                }
            }
            map.put(itemStackKey, false);
            inventory.setInventorySlotContents(slot, currentStack);
        }
        //nothing matched, so return null
        return false;
    }

    private boolean simulateExtractItem(ItemStackKey itemStack) {
        int amountToExtract = requiredItems.getOrDefault(itemStack, 0) + 1;
        int extracted = itemSources.extractItem(itemStack, amountToExtract, true);
        if (extracted == amountToExtract) {
            requiredItems.put(itemStack, amountToExtract);
            return true;
        }
        return false;
    }

    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        if (recipe == null) {
            return false;
        }
        return recipe.matches(inventoryCrafting, world);
    }

    public void setRecipe(IRecipe newRecipe) {
        this.recipe = newRecipe;
        this.replaceAttemptMap.clear();
    }

    public IRecipe getRecipe() {
        return recipe;
    }
}
