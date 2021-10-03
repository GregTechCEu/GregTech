package gregtech.api.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static gregtech.api.util.Predicates.not;
import static gregtech.api.util.Predicates.or;
import static gregtech.api.util.StreamUtils.streamFrom;

public final class InventoryUtils {
    /**
     * @param inventory the target inventory
     * @return the number of empty slots in {@code inventory}
     */
    public static int getNumberOfEmptySlotsInInventory(IItemHandler inventory) {
        // IItemHandler#getSlots() is an int, so this cast is safe.
        return (int)
                streamFrom(inventory)
                        .filter(ItemStack::isEmpty)
                        .count();
    }

    /**
     * Creates a deep copy of the target inventory, optionally keeping empty ItemStacks
     *
     * @param inventory the target inventory
     * @param keepEmpty whether to keep empty ItemStacks (if {@code true}, stacks will be kept).
     * @return a deep copy of the inventory.
     */
    public static List<ItemStack> deepCopy(final IItemHandler inventory,
                                           final boolean keepEmpty) {
        return streamFrom(inventory)
                .filter(or(x -> keepEmpty,
                        not(ItemStack::isEmpty)))
                .map(ItemStack::copy)
                .collect(Collectors.toList());
    }

    /**
     * Determines whether all specified items will fit into a target inventory by
     * simulating merging like items into existing stacks, then checking if there
     * are enough empty stacks left to accommodate the remaining items.
     * <br /><br />
     * <b>Precondition:</b> the target inventory must not virtualize ItemStacks such that
     * they can exceed the maximum stackable size of the item as
     * defined by {@link net.minecraft.item.ItemStack#getMaxStackSize()}.
     * <br /><br />
     * <b>Precondition:</b> the target inventory must actually accept the types of items
     * you are trying to insert.
     * <br /><br />
     *
     * @param inputItems the items you want to insert
     * @param inventory  the target inventory receiving items
     * @return {@code true} if inventory contains sufficient slots to merge and
     * insert all requested items, {@code false} otherwise.
     */
    public static boolean simulateItemStackMerge(List<ItemStack> inputItems,
                                                 IItemHandler inventory) {
        // Generate a stack-minimized copy of the input items
        final List<ItemStack> itemStacks = compactItemStacks(inputItems);

        // If there's enough empty output slots then we don't need to compute merges.
        final int emptySlots = getNumberOfEmptySlotsInInventory(inventory);
        if (itemStacks.size() <= emptySlots)
            return true;

        // Sort by the number of items in each stack so we merge smallest stacks first.
        itemStacks.sort(Comparator.comparingInt(ItemStack::getCount));

        // Deep copy the contents of the target inventory, skipping empty stacks
        final List<ItemStack> inventoryStacks = deepCopy(inventory, false);

        // Perform a merge of the ItemStacks
        mergeItemStacks(itemStacks, inventoryStacks);

        // Return whether there are now sufficient empty slots to fit the unmerged items.
        return itemStacks.size() <= emptySlots;
    }

    /**
     * Compacts multiple ItemStacks into as few stacks as possible. The input items are not modified;
     * the returned stacks are copies.
     *
     * @param inputItems the itemStacks to compact.
     * @return a list containing the resulting ItemStacks.
     */
    static List<ItemStack> compactItemStacks(Collection<ItemStack> inputItems) {
        Hash.Strategy<ItemStack> strategy = ItemStackHashStrategy.comparingAllButCount();
        final Supplier<Map<ItemStack, Integer>> mapSupplier =
                () -> new Object2IntOpenCustomHashMap<>(strategy);

        return inputItems.stream()

                // keep only non-empty item stacks
                .filter(not(ItemStack::isEmpty))

                // Track the number of identical items
                .collect(Collectors.toMap(Function.identity(),
                        ItemStack::getCount,
                        Math::addExact,
                        mapSupplier))

                // Create a single stack of the combined count for each item
                .entrySet().stream()
                .map(entry -> {
                    ItemStack combined = entry.getKey().copy();
                    combined.setCount(entry.getValue());
                    return combined;
                })

                // Normalize these stacks into separate valid ItemStacks, flattening them into a List
                .map(InventoryUtils::normalizeItemStack)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Merges stacks of identical items from a source into a destination.<br />
     * Successfully merged items will be removed from {@code source} and will appear in {@code destination}.<br />
     * Empty stacks in {@code destination} are not considered for this process.
     *
     * @param source      the ItemStacks to merge into {@code destination}.
     * @param destination a target inventory of existing ItemStacks.
     */
    static void mergeItemStacks(Collection<ItemStack> source, Collection<ItemStack> destination) {
        // Since we're mutating the collection during iteration, use an iterator.
        final Iterator<ItemStack> sourceItemStacks = source.iterator();
        while (sourceItemStacks.hasNext()) {
            final ItemStack sourceItemStack = sourceItemStacks.next();

            // Find a matching item in the output bus, if any
            for (ItemStack destItemStack : destination)
                if (ItemStack.areItemsEqual(destItemStack, sourceItemStack) &&
                        ItemStack.areItemStackTagsEqual(destItemStack, sourceItemStack)) {
                    // if it's possible to merge stacks
                    final int availableSlots = destItemStack.getMaxStackSize() - destItemStack.getCount();
                    if (availableSlots > 0) {
                        final int itemCount = Math.min(availableSlots, sourceItemStack.getCount());
                        sourceItemStack.shrink(itemCount);
                        destItemStack.grow(itemCount);

                        // if the output stack was merged completely, remove it and stop looking
                        if (sourceItemStack.isEmpty()) {
                            sourceItemStacks.remove();
                            break;
                        }
                    }
                }
        }
    }

    /**
     * Normalizes a potentially problematic ItemStack by splitting it into copied stacks of at most
     * {@link ItemStack#getMaxStackSize()} items. Empty ItemStacks produce an empty
     * result list, and stacks already in normalized form are copied as a singleton list.
     *
     * @param stack an ItemStack to process.
     * @return an immutable List of the resulting ItemStacks, in descending size order.
     */
    public static List<ItemStack> normalizeItemStack(ItemStack stack) {
        if (stack.isEmpty())
            return Collections.emptyList();

        int maxCount = stack.getMaxStackSize();

        if (stack.getCount() <= maxCount)
            return Collections.singletonList(stack.copy());

        return Collections.unmodifiableList(apportionStack(stack, maxCount));
    }

    /**
     * Divides a stack of items into as many stacks of {@code maxCount} size as possible, followed by
     * a partial stack of any remaining quantity. The original stack is not modified.
     *
     * @param stack    a non-empty ItemStack to split into stacks of at most {@code maxCount} items.
     * @param maxCount the maximum number of items in each stack. Must be a positive, non-zero integer.
     * @return the resulting zero to many whole stacks and zero to one partial stack.
     */
    public static List<ItemStack> apportionStack(ItemStack stack,
                                                 final int maxCount) {
        if (stack.isEmpty())
            throw new IllegalArgumentException("Cannot apportion an empty stack.");
        if (maxCount <= 0)
            throw new IllegalArgumentException("Count must be non-zero and positive.");

        final ArrayList<ItemStack> splitStacks = new ArrayList<>();

        int count = stack.getCount();
        int numStacks = count / maxCount;
        int remainder = count % maxCount;

        for (int fullStackCount = numStacks; fullStackCount > 0; fullStackCount--) {
            ItemStack fullStack = stack.copy();
            fullStack.setCount(maxCount);
            splitStacks.add(fullStack);
        }

        if (remainder > 0) {
            ItemStack partialStack = stack.copy();
            partialStack.setCount(remainder);
            splitStacks.add(partialStack);
        }

        return splitStacks;
    }

    public static InventoryCrafting deepCopyInventoryCrafting(final InventoryCrafting source) {
        final InventoryCrafting result = new InventoryCrafting(new DummyContainer(), source.getWidth(), source.getHeight());
        for (int i = 0; i < result.getSizeInventory(); ++i) {
            result.setInventorySlotContents(i, source.getStackInSlot(i).copy());
        }
        return result;
    }
}
