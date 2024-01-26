package gregtech.common.metatileentities.storage;

import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

public class CachedRecipeData {

//    private final ItemSources itemSources;
    private final IItemHandler handlerList;
    private IRecipe recipe;
    private final Object2IntMap<ItemStack> requiredItems = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());
    private final Object2IntOpenCustomHashMap<ItemStack> stackIndex = new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());

    private final Ingredient[] indexedIngredients = new Ingredient[9];
    private final Int2ObjectMap<Object2BooleanMap<ItemStack>> replaceAttemptMap = new Int2ObjectArrayMap<>();
    private final InventoryCrafting craftingMatrix;

    public CachedRecipeData(final IItemHandler handlerList, IRecipe recipe, InventoryCrafting inventoryCrafting) {
        this.handlerList = handlerList;
        this.recipe = recipe;
        this.craftingMatrix = inventoryCrafting;
    }

    public void updateStackIndex() {
        this.stackIndex.clear();
        for (int i = 0; i < handlerList.getSlots(); i++) {
            var stack = handlerList.extractItem(i, Integer.MAX_VALUE, true);
            if (stack.isEmpty()) continue;
            this.stackIndex.put(stack, i);
        }
    }

    public short attemptMatchRecipe() {
        requiredItems.clear();
        short itemsFound = 0;
        for (int i = 0; i < craftingMatrix.getSizeInventory(); i++) {
            var stack = craftingMatrix.getStackInSlot(i);

            if (indexedIngredients[i] != null && indexedIngredients[i].apply(stack)) {
                int count = requiredItems.getOrDefault(stack, 0);
                requiredItems.put(stack, ++count);
            }

            if (indexedIngredients[i] == null || indexedIngredients[i].apply(stack))
                itemsFound += (short) (1 << i);
        }
        return itemsFound;
    }

    protected boolean consumeRecipeItems() {
        Object2IntMap<ItemStack> gatheredItems = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        if (requiredItems.isEmpty()) {
            return false;
        }
        for (var entry : requiredItems.object2IntEntrySet()) {
            ItemStack stack = entry.getKey();
            int requestedAmount = entry.getIntValue();
            int extractedAmount = 0;
            int index = stackIndex.getOrDefault(stack, -1);
            if (index == -1) continue;
            var extracted = handlerList.extractItem(index, Integer.MAX_VALUE, true).copy();
            extracted.setCount(requestedAmount);
            gatheredItems.put(extracted, index);
            extractedAmount += extracted.getCount();
            if (extractedAmount < requestedAmount) return false;
        }
        for (var gathered : gatheredItems.entrySet()) {
            handlerList.extractItem(gathered.getValue(), gathered.getKey().getCount(), false);
        }
        return !gatheredItems.isEmpty();
//            extractedAmount = itemSources.extractItem(stack, requestedAmount, false);
//            if (extractedAmount != requestedAmount) {
//                gatheredItems.put(stack.copy(), extractedAmount);
//                gathered = false;
//                break;
//            } else {
//                gatheredItems.put(stack.copy(), requestedAmount);
//            }
//        if (!gathered) {
//            for (Object2IntMap.Entry<ItemStack> entry : gatheredItems.object2IntEntrySet()) {
//                itemSources.insertItem(entry.getKey(), entry.getIntValue(), false,
//                        IItemList.InsertMode.HIGHEST_PRIORITY);
//            }
//        }
    }

//    public boolean getIngredientEquivalent(int slot) {
//        ItemStack currentStack = craftingMatrix.getStackInSlot(slot);
//        if (currentStack.isEmpty()) {
//            return true; // stack is empty, nothing to return
//        }
//
//        if (simulateExtractItem(currentStack)) {
//            return true;
//        }
//
//        ItemStack previousStack = recipe.getCraftingResult(craftingMatrix);
//
//        Object2BooleanMap<ItemStack> map = replaceAttemptMap.computeIfAbsent(slot,
//                (m) -> new Object2BooleanOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount()));
//
//        // iterate stored items to find equivalent
//        for (ItemStack itemStack : itemSources.getStoredItems()) {
//            boolean matchedPreviously = false;
//            if (map.containsKey(itemStack)) {
//                if (!map.get(itemStack)) {
//                    continue;
//                } else {
//                    // cant return here before checking if:
//                    // The item is available for extraction
//                    // The recipe output is still the same, as depending on the ingredient, the output NBT may change
//                    matchedPreviously = true;
//                }
//            }
//
//            if (!matchedPreviously) {
//                boolean matched = false;
//                // Matching shapeless recipes actually is very bad for performance, as it checks the entire
//                // recipe ingredients recursively, so we fail early here if none of the recipes ingredients can
//                // take the stack
//                for (Ingredient in : recipe.getIngredients()) {
//                    if (in.apply(itemStack)) {
//                        matched = true;
//                        break;
//                    }
//                }
//                if (!matched) {
//                    map.put(itemStack.copy(), false);
//                    continue;
//                }
//            }
//
//            // update item in slot, and check that recipe matches and output item is equal to the expected one
//            craftingMatrix.setInventorySlotContents(slot, itemStack);
//            if (recipe.matches(craftingMatrix, itemSources.getWorld()) &&
//                    (ItemStack.areItemStacksEqual(recipe.getCraftingResult(craftingMatrix), previousStack) ||
//                            recipe instanceof ShapedOreEnergyTransferRecipe)) {
//                map.put(itemStack, true);
//                // ingredient matched, attempt to extract it and return if successful
//                if (simulateExtractItem(itemStack)) {
//                    return true;
//                }
//            }
//            map.put(itemStack, false);
//            craftingMatrix.setInventorySlotContents(slot, currentStack);
//        }
//        // nothing matched, so return null
//        return false;
//    }

//    private boolean simulateExtractItem(ItemStack itemStack) {
//        int amountToExtract = requiredIngredients.getOrDefault(itemStack, 0) + 1;
//        int extracted = itemSources.extractItem(itemStack, amountToExtract, true);
//        if (extracted == amountToExtract) {
//            requiredIngredients.put(itemStack.copy(), amountToExtract);
//            return true;
//        }
//        return false;
//    }

    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        if (recipe == null) {
            return false;
        }
        return recipe.matches(inventoryCrafting, world);
    }

    public void setRecipe(IRecipe newRecipe) {
        this.recipe = newRecipe;
        this.replaceAttemptMap.clear();
        this.requiredItems.clear();
        if (this.recipe != null) {
//            Ingredient[] indexedIng = new Ingredient[craftingMatrix.getSizeInventory()];
            var ingredients = this.recipe.getIngredients();

//            for (var ing : ingredients) {
//                int count = requiredIngredients.getOrDefault(ing, 0) + 1;
//                requiredIngredients.put(ing, count);
//            }

            int rolling = 0;
            for (int i = 0; i < indexedIngredients.length; i++) {
                indexedIngredients[i] = null;
                var stack = craftingMatrix.getStackInSlot(i);
                if (rolling >= ingredients.size()) break;
                var ingredient = ingredients.get(rolling);
                if (ingredient == Ingredient.EMPTY) {
                    indexedIngredients[i] = ingredient;
                    rolling++;
                    continue;
                }
                if (stack.isEmpty()) continue;
                indexedIngredients[i] = ingredient;
                rolling++;
            }

//            for (int i = 0; i < indexedIng.length; i++) {
//                if (indexedIng[i] == null) continue;
//                var stack = craftingMatrix.getStackInSlot(i);
//                int count = requiredIngredients.getOrDefault(stack, 0) + 1;
//                if (!stack.isEmpty() && indexedIng[i].apply(stack)) {
//                    requiredIngredients.put(stack.copy(), count);
//                }
//            }
        }
    }

    public IRecipe getRecipe() {
        return recipe;
    }
}
