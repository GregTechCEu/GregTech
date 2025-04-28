package gregtech.api.gui.widgets;


import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;

public class ItemSlotTinyAmountTextWidget extends SlotWidget {

    public ItemSlotTinyAmountTextWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition,
                                        int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        ItemStack item = slotReference.getStack();

        if (isMouseOverElement(mouseX, mouseY) && !item.isEmpty()) {
            List<String> tooltip = getItemToolTip(item);

            tooltip.add(TextFormatting.GRAY + I18n.format("gtlitecore.widget.item_slot_tiny_amount.amount_tooltip",
                    TextFormattingUtil.formatNumbers(item.getCount())));

            drawHoveringText(item, tooltip, -1, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture backgroundTexture : this.backgroundTexture) {
                backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        ItemStack itemStack = slotReference.getStack();
        ModularUIGui modularUIGui = gui == null ? null : gui.getModularUIGui();
        if (itemStack.isEmpty() && modularUIGui != null && modularUIGui.getDragSplitting() &&
                modularUIGui.getDragSplittingSlots().contains(slotReference)) { // draw split
            int splitSize = modularUIGui.getDragSplittingSlots().size();
            itemStack = gui.entityPlayer.inventory.getItemStack();
            if (!itemStack.isEmpty() && splitSize > 1 && Container.canAddItemToSlot(slotReference, itemStack, true)) {
                itemStack = itemStack.copy();
                Container.computeStackSize(modularUIGui.getDragSplittingSlots(), modularUIGui.dragSplittingLimit,
                        itemStack, slotReference.getStack().isEmpty() ? 0 : slotReference.getStack().getCount());
                int k = Math.min(itemStack.getMaxStackSize(), slotReference.getItemStackLimit(itemStack));
                if (itemStack.getCount() > k) {
                    itemStack.setCount(k);
                }
            }
        }

        if (!itemStack.isEmpty()) {
            // Render the item with the count set to 1, so it doesn't fill the screen with the amount
            ItemStack renderItem = itemStack.copy();
            renderItem.setCount(1);
            drawItemStack(renderItem, pos.x + 1, pos.y + 1, null);

            // Render the count in a compact manner as to not take up a ton of space
            String amountStr = TextFormattingUtil.formatLongToCompactString(itemStack.getCount(), 4);
            drawStringFixedCorner(amountStr, pos.x + 17, pos.y + 17, 16777215, true, 0.5f);
        }

        if (isMouseOverElement(mouseX, mouseY)) {
            drawSelectionOverlay(pos.x + 1, pos.y + 1, 16, 16);
        }
    }
}
