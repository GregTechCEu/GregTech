package gregtech.common.metatileentities.storage;

import gregtech.api.util.DummyContainer;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;
import gregtech.common.mui.widget.workbench.CraftingInputSlot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.*;

public class CraftingRecipeLogic extends SyncHandler {

    // client only
    public static final int UPDATE_INGREDIENTS = 1;
    public static final int RESET_INGREDIENTS = 2;
    public static final int SYNC_STACK = 4;

    // server only
    public static final int UPDATE_MATRIX = 0;

    private final World world;
    private IItemHandlerModifiable availableHandlers;
    private final Hash.Strategy<ItemStack> strategy = ItemStackHashStrategy.builder()
            .compareItem(true)
            .compareMetadata(true)
            .build();

    /**
     * Used to lookup a list of slots for a given stack
     * filled by {@link CraftingRecipeLogic#refreshStackMap()}
     **/
    private final Map<ItemStack, Set<Integer>> stackLookupMap = new Object2ObjectOpenCustomHashMap<>(this.strategy);

    /**
     * List of items needed to complete the crafting recipe, filled by
     * {@link CraftingRecipeLogic#getIngredientEquivalent(CraftingInputSlot)} )}
     **/
    private final Map<ItemStack, Integer> requiredItems = new Object2IntOpenCustomHashMap<>(
            this.strategy);

    private final Int2IntMap compactedIndexes = new Int2IntArrayMap(9);

    private final Map<Integer, Object2BooleanMap<ItemStack>> replaceAttemptMap = new Int2ObjectArrayMap<>();
    private final InventoryCrafting craftingMatrix;
    private final IInventory craftingResultInventory = new InventoryCraftResult();
    private final CachedRecipeData cachedRecipeData;
    private final CraftingInputSlot[] inputSlots = new CraftingInputSlot[9];

    public CraftingRecipeLogic(World world, IItemHandlerModifiable handlers, IItemHandlerModifiable craftingMatrix) {
        this.world = world;
        this.availableHandlers = handlers;
        this.craftingMatrix = wrapHandler(craftingMatrix);
        this.cachedRecipeData = new CachedRecipeData();
    }

    public IInventory getCraftingResultInventory() {
        return craftingResultInventory;
    }

    public InventoryCrafting getCraftingMatrix() {
        return this.craftingMatrix;
    }

    public void updateInventory(IItemHandlerModifiable handler) {
        this.availableHandlers = handler;
    }

    public void clearCraftingGrid() {
        fillCraftingGrid(Collections.emptyMap());
    }

    public void fillCraftingGrid(Map<Integer, ItemStack> ingredients) {
        for (int i = 0; i < craftingMatrix.getSizeInventory(); i++) {
            craftingMatrix.setInventorySlotContents(i, ingredients.getOrDefault(i, ItemStack.EMPTY));
        }
        syncMatrix();
        updateCurrentRecipe();
    }

    public void setInputSlot(CraftingInputSlot slot, int index) {
        this.inputSlots[index] = slot;
    }

    public boolean performRecipe() {
        return isRecipeValid() && attemptMatchRecipe() && consumeRecipeItems();
    }

    public boolean isRecipeValid() {
        return cachedRecipeData.getRecipe() != null && cachedRecipeData.matches(craftingMatrix, this.world);
    }

    /**
     * Attempts to match the crafting matrix against all connected inventories
     *
     * @return true if all items matched
     */
    public boolean attemptMatchRecipe() {
        this.requiredItems.clear();
        for (CraftingInputSlot slot : this.inputSlots) {
            if (!getIngredientEquivalent(slot)) {
                return false;
            }
        }
        return true;
    }

    protected boolean consumeRecipeItems() {
        if (requiredItems.isEmpty()) {
            return false;
        }
        Map<Integer, Integer> gatheredItems = new Int2IntOpenHashMap();

        for (var entry : requiredItems.entrySet()) {
            ItemStack stack = entry.getKey();
            int requestedAmount = entry.getValue();
            var slotList = stackLookupMap.get(stack);

            int extractedAmount = 0;
            for (int slot : slotList) {
                var extracted = availableHandlers.extractItem(slot, requestedAmount, true);
                gatheredItems.put(slot, extracted.getCount());
                extractedAmount += extracted.getCount();
                requestedAmount -= extracted.getCount();
                if (requestedAmount == 0) break;
            }
            if (extractedAmount < requestedAmount) return false;
        }

        boolean extracted = false;
        for (var gathered : gatheredItems.entrySet()) {
            int slot = gathered.getKey(), amount = gathered.getValue();
            var stack = availableHandlers.getStackInSlot(slot);

            if (stack.isItemStackDamageable()) {
                var usedStack = ForgeHooks.getContainerItem(stack);
                availableHandlers.setStackInSlot(slot, usedStack);
            } else if (stack.getItem().hasContainerItem(stack)) {
                var useStack = stack.getCount() > 1 ? stack.splitStack(1) : stack;
                var newStack = ForgeHooks.getContainerItem(useStack);
                if (newStack.isEmpty()) return false;

                GTTransferUtils.insertItem(this.availableHandlers, newStack, false);
            } else {
                availableHandlers.extractItem(slot, amount, false);
            }
            extracted = true;
        }
        return extracted;
    }

    /**
     * <p>
     * Searches all connected inventories for the slot's stack, and uses
     * {@link CraftingRecipeLogic#findSubstitute(int, ItemStack)} to look for valid substitutes
     * </p>
     * <br/>
     * <p>
     * This method also fills out {@link CraftingRecipeLogic#requiredItems} for use in
     * {@link CraftingRecipeLogic#consumeRecipeItems()}
     * </p>
     *
     * @param slot slot whose current stack to find a substitute for
     * @return true if the stack in the slot can be extracted or has a valid substitute
     */
    public boolean getIngredientEquivalent(CraftingInputSlot slot) {
        ItemStack currentStack = slot.getStack();
        if (currentStack.isEmpty()) {
            return true; // stack is empty, nothing to return
        }

        int count = requiredItems.getOrDefault(currentStack, 0);
        if (simulateExtractItem(currentStack, count + 1)) {
            requiredItems.put(currentStack, ++count);
            return true;
        }

        ItemStack substitute = findSubstitute(slot.getIndex(), currentStack);
        if (substitute.isEmpty()) return false;

        count = requiredItems.getOrDefault(substitute, 0);
        if (simulateExtractItem(substitute, count + 1)) {
            requiredItems.put(substitute, ++count);
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Searches through all connected inventories for a replacement stack that can be used in the recipe
     * </p>
     *
     * @param craftingIndex Index of the current crafting slot
     * @param stack         The stack to find a substitute for
     * @return a valid replacement stack, or {@link ItemStack#EMPTY} if no valid replacements exist
     */
    public ItemStack findSubstitute(int craftingIndex, ItemStack stack) {
        Object2BooleanMap<ItemStack> map = replaceAttemptMap.computeIfAbsent(craftingIndex,
                (m) -> new Object2BooleanOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount()));

        ItemStack substitute = ItemStack.EMPTY;

        var recipe = getCachedRecipe();
        List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
        ingredients.removeIf(ingredient -> ingredient == Ingredient.EMPTY);
        int index = compactedIndexes.get(craftingIndex);

        // iterate stored items to find equivalent
        for (int i = 0; i < this.availableHandlers.getSlots(); i++) {
            var itemStack = availableHandlers.getStackInSlot(i);
            if (itemStack.isEmpty() || this.strategy.equals(itemStack, stack)) continue;

            boolean matchedPreviously = false;
            if (map.containsKey(itemStack)) {
                if (map.get(itemStack)) {
                    // cant return here before checking if:
                    // The item is available for extraction
                    // The recipe output is still the same, as depending on
                    // the ingredient, the output NBT may change
                    matchedPreviously = true;
                }
            }

            if (!matchedPreviously) {
                // Matching shapeless recipes actually is very bad for performance, as it checks the entire
                // recipe ingredients recursively, so we fail early here if none of the recipes ingredients can
                // take the stack
                boolean matched = false;
                if (!(recipe instanceof IShapedRecipe)) {
                    for (Ingredient ing : ingredients) {
                        if (ing.apply(itemStack)) {
                            matched = true;
                            break;
                        }
                    }
                } else {
                    // for shaped recipes, check the exact ingredient instead
                    // ingredients should be in the correct order
                    matched = ingredients.get(index).apply(itemStack);
                }
                if (!matched) {
                    map.put(GTUtility.copy(1, itemStack), false);
                    continue;
                }
            }

            ItemStack previousResult = recipe.getCraftingResult(craftingMatrix);

            // update item in slot, and check that recipe matches and output item is equal to the expected one
            craftingMatrix.setInventorySlotContents(craftingIndex, itemStack);
            var newResult = recipe.getCraftingResult(craftingMatrix);
            if ((cachedRecipeData.matches(craftingMatrix, world) &&
                    ItemStack.areItemStacksEqual(newResult, previousResult)) ||
                    recipe instanceof ShapedOreEnergyTransferRecipe) {
                // ingredient matched, return the substitute
                craftingMatrix.setInventorySlotContents(craftingIndex, stack);
                map.put(GTUtility.copy(1, itemStack), true);
                substitute = itemStack;
                break;
            }
            map.put(itemStack.copy(), false);
            craftingMatrix.setInventorySlotContents(craftingIndex, stack);
        }
        return substitute;
    }

    /**
     * Attempts to extract the given stack from connected inventories
     *
     * @param itemStack stack from the crafting matrix
     * @return true if the stack was successfully extracted or the stack is empty
     */
    private boolean simulateExtractItem(ItemStack itemStack, int count) {
        if (itemStack.isEmpty()) return true;
        if (!stackLookupMap.containsKey(itemStack)) return false;

        int extracted = 0;

        for (int slot : stackLookupMap.get(itemStack)) {
            var slotStack = availableHandlers.extractItem(slot, count, true);
            // we are certain the stack map is correct
            extracted += slotStack.getCount();
            if (extracted >= count) return true;
        }

        return false;
    }

    public void updateCurrentRecipe() {
        if (!cachedRecipeData.matches(craftingMatrix, world)) {
            IRecipe newRecipe = CraftingManager.findMatchingRecipe(craftingMatrix, world);
            ItemStack resultStack = ItemStack.EMPTY;
            if (newRecipe != null) {
                resultStack = newRecipe.getCraftingResult(craftingMatrix);
            }
            this.craftingResultInventory.setInventorySlotContents(0, resultStack);
            this.cachedRecipeData.setRecipe(newRecipe);
        }
    }

    public IRecipe getCachedRecipe() {
        return this.cachedRecipeData.getRecipe();
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        var recipe = getCachedRecipe();
        if (recipe == null) {
            var prevRecipe = cachedRecipeData.getPreviousRecipe();
            if (prevRecipe == null) return;
            cachedRecipeData.setRecipe(null);
            for (CraftingInputSlot inputSlot : this.inputSlots) {
                inputSlot.hasIngredients = true;
            }
            syncToClient(RESET_INGREDIENTS);
            return;
        }

        requiredItems.clear();
        refreshStackMap();
        final Map<Integer, Boolean> map = new Int2BooleanArrayMap();
        int next = 0;
        for (CraftingInputSlot slot : this.inputSlots) {
            final boolean hadIngredients = slot.hasIngredients;

            // check if existing stack works
            var slotStack = slot.getStack();
            if (slotStack.isEmpty()) {
                if (!hadIngredients) {
                    slot.hasIngredients = true;
                    map.put(slot.getIndex(), slot.hasIngredients);
                }
                continue;
            }

            compactedIndexes.put(slot.getIndex(), next++);
            int count = requiredItems.getOrDefault(slotStack, 0) + 1;
            slot.hasIngredients = simulateExtractItem(slotStack, count);

            if (slot.hasIngredients) {
                requiredItems.put(GTUtility.copy(1, slotStack), count);
            } else {
                // check if substitute exists
                ItemStack substitute = findSubstitute(slot.getIndex(), slotStack);
                if (!substitute.isEmpty()) {
                    count = requiredItems.getOrDefault(substitute, 0) + 1;
                    slot.hasIngredients = simulateExtractItem(substitute, count);
                    requiredItems.put(GTUtility.copy(1, substitute), count);
                }
            }

            if (hadIngredients != slot.hasIngredients)
                map.put(slot.getIndex(), slot.hasIngredients);
        }

        // only sync when something has changed
        if (!map.isEmpty()) {
            syncToClient(UPDATE_INGREDIENTS, buffer -> {
                buffer.writeByte(map.size());
                for (var set : map.entrySet()) {
                    buffer.writeByte(set.getKey());
                    buffer.writeBoolean(set.getValue());
                }
            });
        }
    }

    /**
     * Searches available handlers and
     * adds the stack and slots the stack lookup map
     */
    public void refreshStackMap() {
        // the stack lookup map is a pain to do "correctly"
        // so just clear and reset every tick in detectAndSendChanges()
        stackLookupMap.clear();
        for (int i = 0; i < this.availableHandlers.getSlots(); i++) {
            var curStack = this.availableHandlers.getStackInSlot(i);
            if (curStack.isEmpty()) continue;

            Set<Integer> slots;
            if (stackLookupMap.containsKey(curStack)) {
                slots = stackLookupMap.get(curStack);
            } else {
                stackLookupMap.put(GTUtility.copy(1, curStack), slots = new IntArraySet());
            }
            slots.add(i);
        }
    }

    public void writeMatrix(PacketBuffer buffer) {
        buffer.writeVarInt(craftingMatrix.getSizeInventory());
        for (int i = 0; i < craftingMatrix.getSizeInventory(); i++) {
            NetworkUtils.writeItemStack(buffer, craftingMatrix.getStackInSlot(i));
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == UPDATE_INGREDIENTS) {
            int size = buf.readByte();
            for (int i = 0; i < size; i++) {
                this.inputSlots[buf.readByte()].hasIngredients = buf.readBoolean();
            }
        } else if (id == SYNC_STACK) {
            getSyncManager().setCursorItem(NetworkUtils.readItemStack(buf));
        } else if (id == RESET_INGREDIENTS) {
            for (CraftingInputSlot inputSlot : this.inputSlots) {
                inputSlot.hasIngredients = true;
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == UPDATE_MATRIX) {
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                this.craftingMatrix.setInventorySlotContents(i, NetworkUtils.readItemStack(buf));
            }
            this.updateCurrentRecipe();
        }
    }

    public void syncMatrix() {
        if (getSyncManager().isClient())
            syncToServer(UPDATE_MATRIX, this::writeMatrix);
    }

    public static InventoryCrafting wrapHandler(IItemHandlerModifiable handler) {
        return new InventoryCrafting(new DummyContainer(), 3, 3) {

            @Override
            public ItemStack getStackInRowAndColumn(int row, int column) {
                int index = row + (3 * column);
                return handler.getStackInSlot(index);
            }

            @Override
            public ItemStack getStackInSlot(int index) {
                return handler.getStackInSlot(index);
            }

            @Override
            public void setInventorySlotContents(int index, ItemStack stack) {
                handler.setStackInSlot(index, GTUtility.copy(1, stack));
            }
        };
    }
}
