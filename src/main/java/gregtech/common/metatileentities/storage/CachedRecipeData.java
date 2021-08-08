package gregtech.common.metatileentities.storage;

import gregtech.api.util.DummyContainer;
import gregtech.api.util.InventoryUtils;
import gregtech.api.util.ItemStackKey;
import gregtech.common.inventory.IItemList.InsertMode;
import gregtech.common.inventory.itemsource.ItemSourceList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CachedRecipeData {
    private final ItemSourceList itemSourceList;
    private final IRecipe recipe;
    private final ItemStack expectedOutput;
    public final InventoryCrafting inventory = new InventoryCrafting(new DummyContainer(), 3, 3);
    private final Map<ItemStackKey, Integer> requiredItems = new HashMap<>();
    private boolean ingredientsMatched = false;
    private long lastTickChecked = -1L;
    private boolean recipeValidCache = false;

    public CachedRecipeData(ItemSourceList sourceList, IRecipe recipe, ItemStack expectedOutput) {
        this.itemSourceList = sourceList;
        this.recipe = recipe;
        this.expectedOutput = expectedOutput.copy();
    }

    public boolean performRecipe(EntityPlayer player) {
        this.lastTickChecked = -1L;
        if (!checkRecipeValid()) {
            return false;
        }
        if (!consumeRecipeItems(false)) {
            this.lastTickChecked = -1L;
            return false;
        }
        ForgeHooks.setCraftingPlayer(player);
        InventoryCrafting deepCopy = InventoryUtils.deepCopyInventoryCrafting(inventory);
        NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(deepCopy);
        ForgeHooks.setCraftingPlayer(null);
        for (ItemStack itemStack : remainingItems) {
            itemStack = itemStack.copy();
            ItemStackKey stackKey = new ItemStackKey(itemStack);
            int remainingAmount = itemStack.getCount() - itemSourceList.insertItem(stackKey, itemStack.getCount(), false, InsertMode.HIGHEST_PRIORITY);
            if (remainingAmount > 0) {
                itemStack.setCount(remainingAmount);
                player.addItemStackToInventory(itemStack);
                if (itemStack.getCount() > 0) {
                    player.dropItem(itemStack, false, false);
                }
            }
        }
        this.lastTickChecked = -1L;
        return true;
    }

    public boolean attemptMatchRecipe() {
        this.ingredientsMatched = false;
        this.requiredItems.clear();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (!getIngredientEquivalent(i))
                return false; //ingredient didn't match, return false
        }
        this.ingredientsMatched = true;
        return true;
    }

    public boolean checkRecipeValid() {
        if (!ingredientsMatched) {
            return false;
        }
        long currentTick = itemSourceList.getWorld().getTotalWorldTime();
        //check only once a tick if multiple players are viewing crafting table inventory
        if (this.lastTickChecked == currentTick) {
            return recipeValidCache;
        }
        this.lastTickChecked = currentTick;
        this.recipeValidCache = consumeRecipeItems(true);
        return recipeValidCache;
    }

    private boolean consumeRecipeItems(boolean simulate) {
        for (Entry<ItemStackKey, Integer> entry : requiredItems.entrySet()) {
            ItemStackKey itemStackKey = entry.getKey();
            if (itemSourceList.extractItem(itemStackKey, entry.getValue(), simulate) != entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private boolean getIngredientEquivalent(int slot) {
        ItemStack currentStack = inventory.getStackInSlot(slot);
        if (currentStack.isEmpty()) {
            return true; //stack is empty, nothing to return
        }
        ItemStackKey currentStackKey = new ItemStackKey(currentStack);
        if (simulateExtractItem(currentStackKey)) {
            //we can extract ingredient equal to the one in the crafting grid,
            //so just return it without searching equivalent
            return true;
        }
        //iterate stored items to find equivalent
        for (ItemStackKey itemStackKey : itemSourceList.getStoredItems()) {
            ItemStack itemStack = itemStackKey.getItemStack();
            //update item in slot, and check that recipe matches and output item is equal to the expected one
            inventory.setInventorySlotContents(slot, itemStack);
            if (recipe.matches(inventory, itemSourceList.getWorld()) &&
                    ItemStack.areItemStacksEqual(expectedOutput, recipe.getCraftingResult(inventory))) {
                //ingredient matched, attempt to extract it and return if successful
                if (simulateExtractItem(itemStackKey)) {
                    return true;
                }
            }
        }
        //nothing matched, so return null
        return false;
    }

    private boolean simulateExtractItem(ItemStackKey itemStack) {
        int amountToExtract = requiredItems.getOrDefault(itemStack, 0) + 1;
        int extracted = itemSourceList.extractItem(itemStack, amountToExtract, true);
        if (extracted == amountToExtract) {
            requiredItems.put(itemStack, amountToExtract);
            return true;
        }
        return false;
    }
}
