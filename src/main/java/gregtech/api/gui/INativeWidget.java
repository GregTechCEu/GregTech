package gregtech.api.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Native widget is widget wrapping native Slot
 * That means controls are delegated to vanilla {@link net.minecraft.client.gui.inventory.GuiContainer}
 * Rendering is still handled by widget via helpers in {@link gregtech.api.gui.IRenderContext}
 */
public interface INativeWidget {
    /**
     * You should return MC slot handle instance you created earlier
     *
     * @return MC slot
     */
    Slot getHandle();

    /**
     * @return true if this slot belongs to player inventory
     */
    SlotLocationInfo getSlotLocationInfo();

    /**
     * @return true when this slot is valid for double click merging
     */
    boolean canMergeSlot(ItemStack stack);

    /**
     * Called when item is taken from the slot
     * Simulated take is used to compute slot merging behavior
     * This method should not modify slot state if it is simulated
     */
    default ItemStack onItemTake(EntityPlayer player, ItemStack stack, boolean simulate) {
        return stack;
    }

    /**
     * Called when slot is clicked in Container
     * Return null to fallback to vanilla logic
     */
    ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player);

    class SlotLocationInfo {
        public final boolean isPlayerInventory;
        public final boolean isHotbarSlot;

        public SlotLocationInfo(boolean isPlayerInventory, boolean isHotbarSlot) {
            this.isPlayerInventory = isPlayerInventory;
            this.isHotbarSlot = isHotbarSlot;
        }
    }
}
