package gregtech.common.metatileentities.storage;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.util.DummyContainer;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;
import gregtech.common.mui.widget.workbench.CraftingInputSlot;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CraftingRecipeLogic extends SyncHandler {

    private final World world;
    private IItemHandlerModifiable availableHandlers;
    private final Hash.Strategy<ItemStack> strategy = ItemStackHashStrategy.comparingAllButCount();

    /**
     * Used to lookup a list of slots for a given stack
     * filled by {@link CraftingRecipeLogic#handleCacheMiss(ItemStack)}
     **/
    private final Map<ItemStack, Set<Integer>> stackLookupMap = new Object2ObjectOpenCustomHashMap<>(this.strategy);

    /**
     * List of items needed to complete the crafting recipe, filled by
     * {@link CraftingRecipeLogic#getIngredientEquivalent(CraftingInputSlot)} )}
     **/
    private final Map<ItemStack, Integer> requiredItems = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

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
            slot.hasIngredients = getIngredientEquivalent(slot);
            if (!slot.hasIngredients) {
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
                // i don't know if this is necessary
                if (!this.strategy.equals(extracted, stack)) {
                    handleCacheMiss(stack);
                    continue;
                }
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
                int damage = 1;
                if (stack.getItem() instanceof IGTTool gtTool) {
                    damage = gtTool.getToolStats().getDamagePerCraftingAction(stack);
                }
                stack.damageItem(damage, getSyncManager().getPlayer());
            } else if (stack.getItem().hasContainerItem(stack)) {
                var useStack = stack.splitStack(1);
                var newStack = useStack.getItem().getContainerItem(useStack);
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

        // iterate stored items to find equivalent
        for (int i = 0; i < this.availableHandlers.getSlots(); i++) {
            var itemStack = availableHandlers.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;

            var recipe = getCachedRecipe();

            boolean matchedPreviously = false;
            if (map.containsKey(itemStack)) {
                if (!map.get(itemStack)) {
                    continue;
                }
                // cant return here before checking if:
                // The item is available for extraction
                // The recipe output is still the same, as depending on
                // the ingredient, the output NBT may change
                matchedPreviously = true;
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

            ItemStack previousResult = recipe.getCraftingResult(craftingMatrix);

            // update item in slot, and check that recipe matches and output item is equal to the expected one
            craftingMatrix.setInventorySlotContents(craftingIndex, itemStack);
            var newResult = recipe.getCraftingResult(craftingMatrix);
            if ((cachedRecipeData.matches(craftingMatrix, world) &&
                    ItemStack.areItemStacksEqual(newResult, previousResult)) ||
                    recipe instanceof ShapedOreEnergyTransferRecipe) {
                // ingredient matched, return the substitute
                map.put(itemStack.copy(), true);
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
        if (!stackLookupMap.containsKey(itemStack))
            return handleCacheMiss(itemStack);
        int toExtract = count;

        Set<Integer> slots = stackLookupMap.get(itemStack);
        List<Integer> toRemove = new IntArrayList();
        if (slots.isEmpty()) stackLookupMap.remove(itemStack);

        for (int slot : slots) {
            var slotStack = availableHandlers.extractItem(slot, count, true);
            // cache is not correct
            if (slotStack.isEmpty() || !this.strategy.equals(slotStack, itemStack)) {
                toRemove.add(slot);
            } else {
                toExtract -= slotStack.getCount();
                if (toExtract <= 0) break;
            }
        }

        if (!toRemove.isEmpty())
            toRemove.forEach(slots::remove);

        return toExtract <= 0 || handleCacheMiss(itemStack);
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
        if (recipe == null) return;

        requiredItems.clear();
        final Map<Integer, Boolean> map = new Int2BooleanArrayMap();
        for (CraftingInputSlot slot : this.inputSlots) {
            final boolean old = slot.hasIngredients;

            // check if existing stack works
            var slotStack = slot.getStack();
            if (slotStack.isEmpty()) {
                slot.hasIngredients = true;
                continue;
            }

            int count = requiredItems.getOrDefault(slotStack, 0);
            requiredItems.put(slotStack.copy(), ++count);
            slot.hasIngredients = simulateExtractItem(slotStack, count);

            // check if substitute exists
            if (!slot.hasIngredients) {
                ItemStack substitute = findSubstitute(slot.getIndex(), slotStack);
                count = requiredItems.getOrDefault(substitute, 0);
                requiredItems.put(substitute.copy(), ++count);
                slot.hasIngredients = !substitute.isEmpty() && simulateExtractItem(substitute, count);
            }

            if (old != slot.hasIngredients)
                map.put(slot.getIndex(), slot.hasIngredients);
        }

        // only sync when something has changed
        if (!map.isEmpty()) {
            syncToClient(1, buffer -> {
                buffer.writeByte(map.size());
                for (var set : map.entrySet()) {
                    buffer.writeByte(set.getKey());
                    buffer.writeBoolean(set.getValue());
                }
            });
        }
    }

    /**
     * searches available handlers for the stack directly and
     * adds the stack and slots the stack lookup map
     * 
     * @param stack stack to check for in available handlers
     * @return true if a suitable item was found
     */
    public boolean handleCacheMiss(ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (int i = 0; i < this.availableHandlers.getSlots(); i++) {
            var curStack = this.availableHandlers.getStackInSlot(i);
            if (curStack.isEmpty()) continue;

            if (this.strategy.equals(stack, curStack)) {
                // container items like buckets or tools might need special behavior maybe?
                var slots = this.stackLookupMap.computeIfAbsent(stack.copy(), k -> new IntArraySet());
                if (slots.add(i)) return true;
            }
        }
        return false;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            int size = buf.readByte();
            for (int i = 0; i < size; i++) {
                this.inputSlots[buf.readByte()].hasIngredients = buf.readBoolean();
            }
        }
        if (id == 4) {
            getSyncManager().setCursorItem(readStackSafe(buf));
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 0) {
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                try {
                    this.craftingMatrix.setInventorySlotContents(i, buf.readItemStack());
                } catch (IOException ignore) {}
            }
        } else if (id == 4) {
            int slot = buf.readVarInt();
            syncToClient(5, buffer -> {
                buffer.writeVarInt(slot);
                writeStackSafe(buffer, availableHandlers.getStackInSlot(slot));
            });
        }
    }

    private static ItemStack readStackSafe(PacketBuffer buffer) {
        var stack = ItemStack.EMPTY;
        try {
            var tag = buffer.readCompoundTag();
            if (tag == null) throw new IOException();
            // GTLog.logger.warn(String.format("Received: %s", tag));
            stack = new ItemStack(tag);
        } catch (IOException ignore) {
            GTLog.logger.warn("A stack was read incorrectly, something is seriously wrong!");
        }
        return stack;
    }

    private static void writeStackSafe(PacketBuffer buffer, ItemStack stack) {
        var tag = stack.serializeNBT();
        // GTLog.logger.warn(String.format("Sent: %s", tag));
        buffer.writeCompoundTag(tag);
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
