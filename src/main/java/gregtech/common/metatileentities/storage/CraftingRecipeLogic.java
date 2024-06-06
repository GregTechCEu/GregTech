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

import com.cleanroommc.modularui.value.sync.SyncHandler;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CraftingRecipeLogic extends SyncHandler {

    private final World world;
    private IItemHandlerModifiable availableHandlers;

    /**
     * Used to lookup a list of slots for a given stack
     * filled by {@link CraftingRecipeLogic#collectAvailableItems()}
     **/
    private final Object2ObjectOpenCustomHashMap<ItemStack, List<Integer>> stackLookupMap = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    /**
     * List of items needed to complete the crafting recipe, filled by
     * {@link CraftingRecipeLogic#getIngredientEquivalent(ItemStack, IntList)} )}
     **/
    private final Map<ItemStack, Integer> requiredItems = new Object2IntOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    private final Map<Integer, Object2BooleanMap<ItemStack>> replaceAttemptMap = new Int2ObjectArrayMap<>();
    private final InventoryCrafting craftingMatrix;
    private final IInventory craftingResultInventory = new InventoryCraftResult();
    private final CachedRecipeData cachedRecipeData;
    public static short ALL_INGREDIENTS_PRESENT = 511;
    private short presenceMatrix = ALL_INGREDIENTS_PRESENT;

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

    /**
     * Attempts to match the crafting matrix against all available inventories
     *
     * @return 511 if all items matched
     */
    public short attemptMatchRecipe() {
        short presenceMatrix = 511;
        requiredItems.clear();
        for (var stack : compressMatrixToList(this.craftingMatrix).entrySet()) {
            int notFound = stack.getValue().size() - getIngredientEquivalent(stack.getKey(), stack.getValue());
            for (int i = notFound - 1; i >= 0; i--) {
                presenceMatrix -= (short) (1 << stack.getValue().get(i));
            }
        }
        return presenceMatrix;
    }

    private Map<ItemStack, IntList> compressMatrixToList(InventoryCrafting craftingMatrix) {
        Map<ItemStack, IntList> map = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        for (int i = 0; i < craftingMatrix.getSizeInventory(); i++) {
            var stack = craftingMatrix.getStackInSlot(i).copy();
            if (stack.isEmpty()) continue;
            IntList slots = map.computeIfAbsent(stack, s -> new IntArrayList());
            slots.add(i);
        }
        return map;
    }

    /**
     * Searches all available inventories for an ingredient equivalent for a stack in the crafting matrix
     *
     * @param currentStack stack to find a substitute for
     * @return number of slots for which a valid substitute exists
     */
    public int getIngredientEquivalent(ItemStack currentStack, IntList slots) {
        if (currentStack.isEmpty()) {
            return slots.size(); // stack is empty, nothing to return
        }

        if (simulateExtractItem(currentStack, slots.size()) == 0) {
            return slots.size();
        }

        var recipe = getCachedRecipe();

        ItemStack previousStack = recipe.getCraftingResult(craftingMatrix);
        int succeeded = 0;
        for (int slot : slots) {

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
                        succeeded += slots.size() - simulateExtractItem(itemStack, slots.size());
                        if (succeeded == slots.size()) {
                            return slots.size();
                        }
                    }
                    map.put(itemStack, false);
                    craftingMatrix.setInventorySlotContents(slot, currentStack);
                }
            }
        }
        return succeeded;
    }

    /**
     * Attempts to extract the given stack from connected inventories
     *
     * @param itemStack stack from the crafting matrix
     * @param extract the amount to extract
     * @return number of items yet to be extracted
     */
    private int simulateExtractItem(ItemStack itemStack, int extract) {
        if (!stackLookupMap.containsKey(itemStack)) return extract;
        int remaining = extract;
        for (int slot : stackLookupMap.get(itemStack)) {
            var slotStack = availableHandlers.extractItem(slot, remaining, true);
            if (slotStack.getCount() <= remaining) {
                remaining -= slotStack.getCount();
                if (remaining == 0) {
                    requiredItems.put(itemStack, extract);
                    return 0;
                }
            }
        }
        return remaining;
    }

    public boolean performRecipe() {
        if (!isRecipeValid()) return false;

        if (attemptMatchRecipe() != ALL_INGREDIENTS_PRESENT || !consumeRecipeItems()) {
            return false;
        }

        var cachedRecipe = cachedRecipeData.getRecipe();
        var player = getSyncManager().getPlayer();
        ForgeHooks.setCraftingPlayer(player);
        // todo right here is where tools get damaged (in UI)
        NonNullList<ItemStack> remainingItems = cachedRecipe.getRemainingItems(craftingMatrix);
        ForgeHooks.setCraftingPlayer(null);
        for (ItemStack itemStack : remainingItems) {
            if (itemStack.isEmpty()) {
                continue;
            }

            int remainingAmount = GTTransferUtils.insertItem(this.availableHandlers, itemStack, true).getCount();
            if (remainingAmount > 0) {
                itemStack.setCount(remainingAmount);
                if (!player.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false, false);
                }
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
                int damage = 1;
                if (stack.getItem() instanceof IGTTool gtTool) {
                    damage = gtTool.getToolStats().getDamagePerCraftingAction(stack);
                }
                stack.damageItem(damage, getSyncManager().getPlayer());
            } else if (stack.getItem().hasContainerItem(stack)) {
                var newStack = stack.getItem().getContainerItem(stack);
                availableHandlers.setStackInSlot(slot, newStack);
            } else {
                availableHandlers.extractItem(slot, amount, false);
            }
            extracted = true;
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

    @Override
    public void detectAndSendChanges(boolean init) {
        super.detectAndSendChanges(init);
        this.collectAvailableItems();
        short newMatrix;
        if (getCachedRecipeData().getRecipe() != null) {
            newMatrix = attemptMatchRecipe();
        } else {
            newMatrix = ALL_INGREDIENTS_PRESENT;
        }
        if (init || newMatrix != this.presenceMatrix) {
            this.presenceMatrix = newMatrix;
            syncToClient(1, buffer -> {
                buffer.writeShort(presenceMatrix);
            });
        }
    }

    public short getTintLocations() {
        return (short) (ALL_INGREDIENTS_PRESENT - presenceMatrix);
    }

    public CachedRecipeData getCachedRecipeData() {
        return this.cachedRecipeData;
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
            this.presenceMatrix = buf.readShort();
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
