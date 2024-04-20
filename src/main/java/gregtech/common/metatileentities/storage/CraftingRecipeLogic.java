package gregtech.common.metatileentities.storage;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.util.DummyContainer;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("OverrideOnly") // stupid annotations conflicting with each other
public class CraftingRecipeLogic extends SyncHandler {

    private final World world;
    private IItemHandlerModifiable availableHandlers;

    /** Used to lookup a list of slots for a given stack */
    private final Object2ObjectOpenCustomHashMap<ItemStack, List<Integer>> stackLookupMap = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    /**
     * List of items needed to complete the crafting recipe,
     * filled by {@link CraftingRecipeLogic#getIngredientEquivalent(int)}
     **/
    private final Map<ItemStack, Integer> requiredItems = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    private final Map<Integer, Object2BooleanMap<ItemStack>> replaceAttemptMap = new Int2ObjectArrayMap<>();
    private final InventoryCrafting craftingMatrix;
    private final IInventory craftingResultInventory = new InventoryCraftResult();
    private final CachedRecipeData cachedRecipeData;
    public static short ALL_INGREDIENTS_PRESENT = 511;
    private short tintLocation = ALL_INGREDIENTS_PRESENT;

    public CraftingRecipeLogic(World world, IItemHandlerModifiable handlers, IItemHandlerModifiable craftingMatrix) {
        this.world = world;
        this.availableHandlers = handlers;
        this.craftingMatrix = new CraftingWrapper(craftingMatrix);
        this.cachedRecipeData = new CachedRecipeData();
    }

    @Override
    public void init(String key, GuiSyncManager syncManager) {
        super.init(key, syncManager);
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

    /**
     * Attempts to match the crafting matrix against all available inventories
     * 
     * @return true if all items matched
     */
    public boolean attemptMatchRecipe() {
        requiredItems.clear();
        short itemsFound = 0;
        for (int i = 0; i < craftingMatrix.getSizeInventory(); i++) {
            if (getIngredientEquivalent(i))
                itemsFound += (short) (1 << i);
        }
        return itemsFound == ALL_INGREDIENTS_PRESENT;
    }

    /**
     * Searches all available inventories for an ingredient equivalent for a stack in the crafting matrix
     * 
     * @param slot index of the crafting matrix
     * @return true if a valid substitute exists for the stack in the slot
     */
    public boolean getIngredientEquivalent(int slot) {
        ItemStack currentStack = craftingMatrix.getStackInSlot(slot).copy();
        if (currentStack.isEmpty()) {
            return true; // stack is empty, nothing to return
        }

        if (simulateExtractItem(currentStack)) {
            return true;
        }

        var recipe = getCachedRecipe();

        ItemStack previousStack = recipe.getCraftingResult(craftingMatrix);

        Object2BooleanMap<ItemStack> map = replaceAttemptMap.computeIfAbsent(slot,
                (m) -> new Object2BooleanOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount()));

        // iterate stored items to find equivalent
        for (var entry : stackLookupMap.entrySet()) {
            for (int i : entry.getValue()) {
                var itemStack = availableHandlers.getStackInSlot(i);

                boolean matchedPreviously = false;
                if (map.containsKey(itemStack)) {
                    if (!map.get(itemStack)) {
                        continue;
                    } else {
                        // cant return here before checking if:
                        // The item is available for extraction
                        // The recipe output is still the same, as depending on the ingredient, the output NBT may
                        // change
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
                craftingMatrix.setInventorySlotContents(slot, itemStack);
                if ((cachedRecipeData.matches(craftingMatrix, world) &&
                        ItemStack.areItemStacksEqual(recipe.getCraftingResult(craftingMatrix), previousStack)) ||
                        recipe instanceof ShapedOreEnergyTransferRecipe) {
                    map.put(itemStack, true);
                    // ingredient matched, attempt to extract it and return if successful
                    if (simulateExtractItem(itemStack)) {
                        return true;
                    }
                }
                map.put(itemStack, false);
                craftingMatrix.setInventorySlotContents(slot, currentStack);
            }
        }
        // nothing matched, so return null
        return false;
    }

    /**
     * Attempts to extract the given stack from connected inventories
     * 
     * @param itemStack - stack from the crafting matrix
     * @return true if the item exists in available inventories
     */
    private boolean simulateExtractItem(ItemStack itemStack) {
        int amountToExtract = requiredItems.getOrDefault(itemStack, 0) + 1;
        if (!stackLookupMap.containsKey(itemStack)) return false;

        var extracted = ItemStack.EMPTY;
        for (int slot : stackLookupMap.get(itemStack)) {
            extracted = availableHandlers.extractItem(slot, amountToExtract, true).copy();
            if (extracted.getCount() == amountToExtract) {
                requiredItems.put(extracted, amountToExtract);
                return true;
            }
        }
        return false;
    }

    public void performRecipe() {
        if (!isRecipeValid()) return;

        if (!getSyncManager().isClient())
            syncToClient(1, this::writeAvailableStacks);

        if (!attemptMatchRecipe() || !consumeRecipeItems(false)) {
            return;
        }

        // updateClientCraft();
        syncToClient(4, buffer -> writeStackSafe(buffer, getSyncManager().getCursorItem()));

        var cachedRecipe = cachedRecipeData.getRecipe();
        var player = getSyncManager().getPlayer();
        ForgeHooks.setCraftingPlayer(player);
        // todo right here is where tools get damaged (in UI)
        NonNullList<ItemStack> remainingItems = cachedRecipe.getRemainingItems(craftingMatrix);
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < remainingItems.size(); i++) {
            ItemStack itemStack = remainingItems.get(i);
            if (itemStack.isEmpty()) {
                continue;
            }

            // ItemStack current = craftingMatrix.getStackInSlot(i);
            // craftingMatrix.setInventorySlotContents(i, itemStack);
            // if (!cachedRecipe.matches(craftingMatrix, this.world)) {
            // craftingMatrix.setInventorySlotContents(i, current);
            // }

            int remainingAmount = GTTransferUtils.insertItem(this.availableHandlers, itemStack, true).getCount();
            if (remainingAmount > 0) {
                itemStack.setCount(remainingAmount);
                if (!player.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false, false);
                }
            }
        }
    }

    protected boolean consumeRecipeItems(boolean simulate) {
        if (requiredItems.isEmpty()) {
            return false;
        }
        Object2IntMap<ItemStack> gatheredItems = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (var entry : requiredItems.entrySet()) {
            ItemStack stack = entry.getKey();
            int requestedAmount = entry.getValue();
            var slotList = stackLookupMap.getOrDefault(stack, new IntArrayList());
            if (slotList.size() == 0) {
                continue;
            }
            int extractedAmount = 0;
            for (int slot : slotList) {
                var extracted = availableHandlers.extractItem(slot, requestedAmount, true);
                gatheredItems.put(extracted, slot);
                extractedAmount += extracted.getCount();
                requestedAmount -= extractedAmount;
                if (requestedAmount == 0) break;
            }
            if (extractedAmount < requestedAmount) return false;
        }

        boolean extracted = false;
        for (var gathered : gatheredItems.entrySet()) {
            var stack = gathered.getKey();
            int slot = gathered.getValue();
            if (stack.isEmpty()) {
                stackLookupMap.get(stack).remove(slot);
            } else {
                if (stack.getItem().hasContainerItem(stack) && stack.isItemStackDamageable()) {
                    int damage = 1;
                    if (stack.getItem() instanceof IGTTool gtTool) {
                        damage = gtTool.getToolStats().getDamagePerCraftingAction(stack);
                    }
                    if (!simulate) stack.damageItem(damage, getSyncManager().getPlayer());
                } else {
                    availableHandlers.extractItem(slot, stack.getCount(), simulate);
                }
                extracted = true;
            }
        }
        return extracted;
    }

    public boolean isRecipeValid() {
        return cachedRecipeData.getRecipe() != null && cachedRecipeData.matches(craftingMatrix, this.world);
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

    public void update() {
        if (getCachedRecipeData().getRecipe() != null) {
            // todo fix tint location
            // tintLocation = getCachedRecipeData().attemptMatchRecipe();
        } else {
            tintLocation = ALL_INGREDIENTS_PRESENT;
        }
    }

    public short getTintLocations() {
        return tintLocation;
    }

    public CachedRecipeData getCachedRecipeData() {
        return this.cachedRecipeData;
    }

    public void writeAvailableStacks(PacketBuffer buffer) {
        this.collectAvailableItems();
        Map<Integer, ItemStack> written = new Int2ObjectArrayMap<>();
        for (var slots : this.stackLookupMap.entrySet()) {
            for (var slot : slots.getValue()) {
                var stack = this.availableHandlers.getStackInSlot(slot);
                written.put(slot, stack);
            }
        }

        buffer.writeInt(written.size());
        for (var entry : written.entrySet()) {
            buffer.writeInt(entry.getKey());
            writeStackSafe(buffer, entry.getValue());
        }
    }

    public void collectAvailableItems() {
        this.stackLookupMap.clear();
        for (int i = 0; i < this.availableHandlers.getSlots(); i++) {
            var stack = this.availableHandlers.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            this.stackLookupMap
                    .computeIfAbsent(stack, k -> new IntArrayList())
                    .add(i);
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            // updateClientStacks(buf);
        } else if (id == 3) {
            syncToServer(3);
        } else if (id == 4) {
            getSyncManager().setCursorItem(readStackSafe(buf));
        } else if (id == 5) {
            int slot = buf.readVarInt();
            var stack = readStackSafe(buf);
            this.availableHandlers.setStackInSlot(slot, stack);
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
        } else if (id == 1) {
            syncToClient(1, this::writeAvailableStacks);
        } else if (id == 3) {
            // syncToClient(1, this::writeAvailableStacks);
        } else if (id == 4) {
            int slot = buf.readVarInt();
            syncToClient(5, buffer -> {
                buffer.writeVarInt(slot);
                writeStackSafe(buffer, availableHandlers.getStackInSlot(slot));
            });
        }
    }

    public void updateClientStacks(PacketBuffer buffer) {
        this.stackLookupMap.clear();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            int slot = buffer.readInt();
            var serverStack = readStackSafe(buffer);
            var clientStack = this.availableHandlers.extractItem(slot, Integer.MAX_VALUE, true);

            if (clientStack.isEmpty() || !ItemStack.areItemStacksEqual(clientStack, serverStack)) {
                this.availableHandlers.extractItem(slot, Integer.MAX_VALUE, false);
                this.availableHandlers.insertItem(slot, serverStack.copy(), false);
            }

            this.stackLookupMap
                    .computeIfAbsent(serverStack, k -> new IntArrayList())
                    .add(slot);
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

    // public void updateClientCraft() {
    // var curStack = getSyncManager().getCursorItem();
    // var outStack = getCachedRecipe().getRecipeOutput();
    // if (curStack.isEmpty()) {
    // getSyncManager().setCursorItem(outStack);
    // } else if (ItemStack.areItemStacksEqual(curStack, outStack)) {
    // curStack.grow(outStack.getCount());
    // }
    // }

    private static void writeStackSafe(PacketBuffer buffer, ItemStack stack) {
        var tag = stack.serializeNBT();
        // GTLog.logger.warn(String.format("Sent: %s", tag));
        buffer.writeCompoundTag(tag);
    }

    private static class CraftingWrapper extends InventoryCrafting {

        IItemHandlerModifiable craftingHandler;

        public CraftingWrapper(IItemHandlerModifiable craftingHandler) {
            super(new DummyContainer(), 3, 3);
            this.craftingHandler = craftingHandler;
        }

        @Override
        public ItemStack getStackInRowAndColumn(int row, int column) {
            int index = row + (3 * column);
            return this.craftingHandler.getStackInSlot(index);
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return craftingHandler.getStackInSlot(index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            craftingHandler.setStackInSlot(index, GTUtility.copy(1, stack));
        }
    }
}
