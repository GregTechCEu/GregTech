package gregtech.common.metatileentities.storage;

import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.itemsource.ItemSources;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

public class CachedRecipeData {

    private final ItemSources itemSources;
    private IRecipe recipe;
    private final Object2IntMap<ItemStack> requiredItems = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());
    private final Int2ObjectMap<Object2BooleanMap<ItemStack>> replaceAttemptMap = new Int2ObjectArrayMap<>();
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
                itemsFound += 1 << i; // ingredient was found, and indicate in the short of this fact
        }
        if (itemsFound != CraftingRecipeLogic.ALL_INGREDIENTS_PRESENT) {
            requiredItems.clear();
        }
        return itemsFound;
    }

    protected boolean consumeRecipeItems() {
        boolean gathered = true;
        Object2IntMap<ItemStack> gatheredItems = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        if (requiredItems.isEmpty()) {
            return false;
        }
        for (Object2IntMap.Entry<ItemStack> entry : requiredItems.object2IntEntrySet()) {
            ItemStack stack = entry.getKey();
            int requestedAmount = entry.getIntValue();
            int extractedAmount = itemSources.extractItem(stack, requestedAmount, false);
            if (extractedAmount != requestedAmount) {
                gatheredItems.put(stack.copy(), extractedAmount);
                gathered = false;
                break;
            } else {
                gatheredItems.put(stack.copy(), requestedAmount);
            }
        }
        if (!gathered) {
            for (Object2IntMap.Entry<ItemStack> entry : gatheredItems.object2IntEntrySet()) {
                itemSources.insertItem(entry.getKey(), entry.getIntValue(), false,
                        IItemList.InsertMode.HIGHEST_PRIORITY);
            }
        }
        return gathered;
    }

    public boolean getIngredientEquivalent(int slot) {
        ItemStack currentStack = inventory.getStackInSlot(slot);
        if (currentStack.isEmpty()) {
            return true; // stack is empty, nothing to return
        }

        if (simulateExtractItem(currentStack)) {
            return true;
        }

        ItemStack previousStack = recipe.getCraftingResult(inventory);

        Object2BooleanMap<ItemStack> map = replaceAttemptMap.computeIfAbsent(slot,
                (m) -> new Object2BooleanOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount()));

        // iterate stored items to find equivalent
        for (ItemStack itemStack : itemSources.getStoredItems()) {
            boolean matchedPreviously = false;
            if (map.containsKey(itemStack)) {
                if (!map.get(itemStack)) {
                    continue;
                } else {
                    // cant return here before checking if:
                    // The item is available for extraction
                    // The recipe output is still the same, as depending on the ingredient, the output NBT may change
                    matchedPreviously = true;
                }
            }

            if (!matchedPreviously) {
                boolean matched = false;
                // Matching shapeless recipes actually is very bad for performance, as it checks the entire
                // recipe ingredients recursively, so we fail early here if none of the recipes ingredients can
                // take the stack
                for (Ingredient in : recipe.getIngredients()) {
                    if (in.apply(itemStack)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    map.put(itemStack.copy(), false);
                    continue;
                }
            }

            // update item in slot, and check that recipe matches and output item is equal to the expected one
            inventory.setInventorySlotContents(slot, itemStack);
            if (recipe.matches(inventory, itemSources.getWorld()) &&
                    (ItemStack.areItemStacksEqual(recipe.getCraftingResult(inventory), previousStack) ||
                            recipe instanceof ShapedOreEnergyTransferRecipe)) {
                map.put(itemStack, true);
                // ingredient matched, attempt to extract it and return if successful
                if (simulateExtractItem(itemStack)) {
                    return true;
                }
            }
            map.put(itemStack, false);
            inventory.setInventorySlotContents(slot, currentStack);
        }
        // nothing matched, so return null
        return false;
    }

    private boolean simulateExtractItem(ItemStack itemStack) {
        int amountToExtract = requiredItems.getOrDefault(itemStack, 0) + 1;
        int extracted = itemSources.extractItem(itemStack, amountToExtract, true);
        if (extracted == amountToExtract) {
            requiredItems.put(itemStack.copy(), amountToExtract);
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
