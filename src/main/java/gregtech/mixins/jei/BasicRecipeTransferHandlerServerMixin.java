package gregtech.mixins.jei;

import gregtech.api.items.toolitem.ToolHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(BasicRecipeTransferHandlerServer.class)
public class BasicRecipeTransferHandlerServerMixin {

    /**
     * @author M_W_K
     * @reason The modification required to make tools only be needed once on a max-transfer event is best done through
     *         an overwrite, due to being spread out throughout the method.
     */
    @Overwrite(remap = false)
    private static Map<Integer, ItemStack> removeItemsFromInventory(
                                                                    EntityPlayer player,
                                                                    Container container,
                                                                    Map<Integer, ItemStack> required,
                                                                    List<Integer> craftingSlots,
                                                                    List<Integer> inventorySlots,
                                                                    boolean transferAsCompleteSets,
                                                                    boolean maxTransfer) {
        List<Map.Entry<Integer, ItemStack>> orderedRequired = new ObjectArrayList<>(required.entrySet());

        // This map becomes populated with the resulting items to transfer and is returned by this method.
        final Map<Integer, ItemStack> result = new HashMap<>(orderedRequired.size());

        loopSets:
        while (true) { // for each set

            // This map holds the original contents of a slot we have removed items from. This is used if we don't
            // have enough items to complete a whole set, we can roll back the items that were removed.
            Map<Slot, ItemStack> originalSlotContents = null;

            if (transferAsCompleteSets) {
                // We only need to create a new map for each set iteration if we're transferring as complete sets.
                originalSlotContents = new HashMap<>();
            }

            // This map holds items found for each set iteration. Its contents are added to the result map
            // after each complete set iteration. If we are transferring as complete sets, this allows
            // us to simply ignore the map's contents when a complete set isn't found.
            final Map<Integer, ItemStack> foundItemsInSet = new HashMap<>(orderedRequired.size());

            // This flag is set to false if at least one item is found during the set iteration. It is used
            // to determine if iteration should continue and prevents an infinite loop if not transferring
            // as complete sets.
            boolean noItemsFound = true;

            for (int i = 0; i < orderedRequired.size(); i++) {
                Map.Entry<Integer, ItemStack> entry = orderedRequired.get(i);
                if (entry == null) continue; // null entries are set for tool stacks that have been satisfied

                final ItemStack requiredStack = entry.getValue().copy();

                // Locate a slot that has what we need.
                final Slot slot = getSlotWithStack(container, requiredStack, craftingSlots, inventorySlots);

                boolean itemFound = (slot != null) && !slot.getStack().isEmpty() && slot.canTakeStack(player);
                ItemStack resultItemStack = result.get(entry.getKey());
                boolean resultItemStackLimitReached = (resultItemStack != null) &&
                        (resultItemStack.getCount() == resultItemStack.getMaxStackSize());

                if (!itemFound || resultItemStackLimitReached) {
                    // We can't find any more items to fulfill the requirements or the maximum stack size for this item
                    // has been reached.

                    if (transferAsCompleteSets) {
                        // Since the full set requirement wasn't satisfied, we need to roll back any
                        // slot changes we've made during this set iteration.
                        for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
                            ItemStack stack = slotEntry.getValue();
                            slotEntry.getKey().putStack(stack);
                        }
                        break loopSets;
                    }

                } else { // the item was found and the stack limit has not been reached

                    // Keep a copy of the slot's original contents in case we need to roll back.
                    if (originalSlotContents != null && !originalSlotContents.containsKey(slot)) {
                        originalSlotContents.put(slot, slot.getStack().copy());
                    }

                    // Reduce the size of the found slot.
                    ItemStack removedItemStack = slot.decrStackSize(1);
                    foundItemsInSet.put(entry.getKey(), removedItemStack);

                    // mark tool stacks as 'permanently' satisfied
                    if (ToolHelper.isTool(removedItemStack)) orderedRequired.set(i, null);

                    noItemsFound = false;
                }
            }

            // Merge the contents of the temporary map with the result map.
            for (Map.Entry<Integer, ItemStack> entry : foundItemsInSet.entrySet()) {
                ItemStack resultItemStack = result.get(entry.getKey());

                if (resultItemStack == null) {
                    result.put(entry.getKey(), entry.getValue());

                } else {
                    resultItemStack.grow(1);
                }
            }

            if (!maxTransfer || noItemsFound) {
                // If max transfer is not requested by the player this will exit the loop after trying one set.
                // If no items were found during this iteration, we're done.
                break;
            }
        }

        return result;
    }

    @Shadow(remap = false)
    private static Slot getSlotWithStack(Container container, ItemStack requiredStack, List<Integer> craftingSlots,
                                         List<Integer> inventorySlots) {
        return null;
    }
}
