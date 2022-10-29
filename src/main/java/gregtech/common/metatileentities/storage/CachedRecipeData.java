package gregtech.common.metatileentities.storage;

import gregtech.api.recipes.KeySharedStack;
import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.itemsource.ItemSources;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CachedRecipeData {
    private final ItemSources itemSources;
    private IRecipe recipe;
    private final Map<ItemStackKey, Integer> requiredItems = new HashMap<>();
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

        //iterate stored items to find equivalent
        for (ItemStackKey itemStackKey : itemSources.getStoredItems()) {
            ItemStack itemStack = itemStackKey.getItemStack();
            //update item in slot, and check that recipe matches and output item is equal to the expected one
            inventory.setInventorySlotContents(slot, itemStack);
            if (recipe.matches(inventory, itemSources.getWorld())) {
                //ingredient matched, attempt to extract it and return if successful
                if (simulateExtractItem(itemStackKey)) {
                    return true;
                }
            }
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
    }

    public IRecipe getRecipe() {
        return recipe;
    }
}
