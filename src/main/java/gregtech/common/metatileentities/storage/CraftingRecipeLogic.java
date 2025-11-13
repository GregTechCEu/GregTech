package gregtech.common.metatileentities.storage;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.util.DummyContainer;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;
import gregtech.common.mui.widget.workbench.CraftingInputSlot;

import net.minecraft.entity.player.EntityPlayerMP;
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
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.Collections;
import java.util.Map;

public class CraftingRecipeLogic extends SyncHandler {

    // client only
    public static final int UPDATE_INGREDIENTS = 1;
    public static final int RESET_INGREDIENTS = 2;
    public static final int SYNC_STACK = 3;

    // server only
    public static final int UPDATE_MATRIX = 0;

    private final World world;
    private IItemHandlerModifiable availableHandlers;
    private final ItemStackHashStrategy strategy = ItemStackHashStrategy.builder()
            .compareItem(true)
            .compareMetadata(true)
            .build();

    /**
     * Used to lookup a list of slots for a given stack
     * filled by {@link CraftingRecipeLogic#refreshStackMap()}
     **/
    private final Map<ItemStack, IntSet> stackLookupMap = new Object2ObjectOpenCustomHashMap<>(this.strategy);

    /**
     * List of items needed to complete the crafting recipe, filled by
     * {@link CraftingRecipeLogic#detectAndSendChanges(boolean)} )}
     **/
    private final Object2IntMap<ItemStack> requiredItems = new Object2IntOpenCustomHashMap<>(
            this.strategy);

    private final Int2IntMap compactedIndexes = new Int2IntArrayMap(9);
    private final Int2IntMap slotMap = new Int2IntArrayMap();

    private final Int2ObjectMap<Object2BooleanMap<ItemStack>> replaceAttemptMap = new Int2ObjectArrayMap<>();
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

    public void updateSlotMap(int offset, int slot) {
        slotMap.put(offset + slot, slotMap.size());
    }

    public void clearSlotMap() {
        slotMap.clear();
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
        for (CraftingInputSlot slot : this.inputSlots) {
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
        Int2IntMap gatheredItems = new Int2IntOpenHashMap();

        for (var entry : requiredItems.entrySet()) {
            ItemStack stack = entry.getKey();
            int requestedAmount = entry.getValue();
            var slotList = stackLookupMap.get(stack);

            for (int slot : slotList) {
                var extracted = availableHandlers.extractItem(slot, requestedAmount, true);
                gatheredItems.put(slot, extracted.getCount());
                requestedAmount -= extracted.getCount();
            }
            // not enough to satisfy the recipe, return false
            if (requestedAmount > 0) return false;
        }

        for (var gathered : gatheredItems.entrySet()) {
            int slot = gathered.getKey(), amount = gathered.getValue();
            var stack = availableHandlers.getStackInSlot(slot);
            boolean hasContainer = stack.getItem().hasContainerItem(stack);

            if (!hasContainer) {
                // not a transmutable item (damagable tool, etc), extract normally
                availableHandlers.extractItem(slot, amount, false);
            } else if (stack.getCount() > 1) {
                // only some stacks are transmuted, try insert non-empty stacks
                ItemStack newStack = ForgeHooks.getContainerItem(stack.splitStack(1));
                if (!newStack.isEmpty())
                    GTTransferUtils.insertItem(this.availableHandlers, newStack, false);
            } else {
                // all stacks are transmuted, just replace
                availableHandlers.setStackInSlot(slot, ForgeHooks.getContainerItem(stack));
            }
        }
        // we've checked everything, return true
        return true;
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
        int index = compactedIndexes.get(craftingIndex);

        // iterate stored items to find equivalent
        for (int i = 0; i < this.availableHandlers.getSlots(); i++) {
            var itemStack = availableHandlers.getStackInSlot(i);
            if (itemStack.isEmpty() || this.strategy.equals(itemStack, stack)) continue;

            boolean matchedPreviously = false;
            if (map.containsKey(itemStack)) {
                if (map.getBoolean(itemStack)) {
                    // cant return here before checking if:
                    // The item is available for extraction
                    // The recipe output is still the same, as depending on
                    // the ingredient, the output NBT may change
                    matchedPreviously = true;
                }
            }

            // this is also every tick
            if (itemStack.getItem() instanceof ItemGTToolbelt) {
                // we need to do this here because of ingredient apply
                ItemGTToolbelt.setCraftingSlot(slotMap.get(i), (EntityPlayerMP) getSyncManager().getPlayer());
            }

            if (!matchedPreviously) {
                // Matching shapeless recipes actually is very bad for performance, as it checks the entire
                // recipe ingredients recursively, so we fail early here if none of the recipes ingredients can
                // take the stack
                boolean matched = false;
                if (!(recipe instanceof IShapedRecipe)) {
                    for (Ingredient ing : recipe.getIngredients()) {
                        if (ing.apply(itemStack)) {
                            matched = true;
                            break;
                        }
                    }
                } else {
                    // for shaped recipes, check the exact ingredient instead
                    // ingredients should be in the correct order
                    matched = cachedRecipeData.canIngredientApply(index, itemStack);
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
            // this will send packets every tick for the toolbelt, not sure what can be done
            if ((cachedRecipeData.matches(craftingMatrix, world) &&
                    ItemStack.areItemStacksEqual(newResult, previousResult)) ||
                    recipe instanceof ShapedOreEnergyTransferRecipe) {
                // ingredient matched, return the substitute
                craftingMatrix.setInventorySlotContents(craftingIndex, stack);
                map.put(GTUtility.copy(1, itemStack), true);
                substitute = itemStack;
                break;
            }
            map.put(GTUtility.copy(1, itemStack), false);
            craftingMatrix.setInventorySlotContents(craftingIndex, stack);
        }
        return substitute;
    }

    /**
     * Attempts to extract the given stack from connected inventories
     *
     * @param craftingIndex current crafting index
     * @param itemStack     stack from the crafting matrix
     * @return true if the stack was successfully extracted or the stack is empty
     */
    private boolean simulateExtractItem(int craftingIndex, ItemStack itemStack, int count) {
        if (itemStack.isEmpty()) return true;
        if (!stackLookupMap.containsKey(itemStack)) return false;

        int extracted = 0;

        for (int slot : stackLookupMap.get(itemStack)) {
            var slotStack = availableHandlers.extractItem(slot, count, true);
            // we are certain the stack map is correct
            if (slotStack.getItem() instanceof ItemGTToolbelt) {
                ItemGTToolbelt.setCraftingSlot(slotMap.get(slot), (EntityPlayerMP) getSyncManager().getPlayer());
            }
            if (cachedRecipeData.canIngredientApply(compactedIndexes.get(craftingIndex), slotStack)) {
                extracted += slotStack.getCount();
                if (extracted >= count) return true;
            }
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

        Int2BooleanMap map = updateInputSlots();

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
     * Updates each input slot for if a valid item exists for that slot
     *
     * @return a map of slots that has changed since last time, if any
     */
    private Int2BooleanMap updateInputSlots() {
        compactedIndexes.clear();
        requiredItems.clear();
        refreshStackMap();

        Int2BooleanMap map = new Int2BooleanArrayMap();
        int next = 0;
        for (CraftingInputSlot slot : this.inputSlots) {
            boolean hadIngredients = slot.hasIngredients;

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
            slot.hasIngredients = simulateExtractItem(slot.getIndex(), slotStack, count);

            if (slot.hasIngredients) {
                requiredItems.put(GTUtility.copy(1, slotStack), count);
            } else {
                // check if substitute exists
                ItemStack substitute = findSubstitute(slot.getIndex(), slotStack);
                if (!substitute.isEmpty()) {
                    count = requiredItems.getOrDefault(substitute, 0) + 1;
                    slot.hasIngredients = simulateExtractItem(slot.getIndex(), substitute, count);
                    requiredItems.put(GTUtility.copy(1, substitute), count);
                }
            }

            if (hadIngredients != slot.hasIngredients)
                map.put(slot.getIndex(), slot.hasIngredients);
        }
        return map;
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

            IntSet slots;
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
