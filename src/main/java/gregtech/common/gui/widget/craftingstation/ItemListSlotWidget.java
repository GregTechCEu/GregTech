package gregtech.common.gui.widget.craftingstation;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.KeySharedStack;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.inventory.IItemInfo;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.IItemList.InsertMode;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class ItemListSlotWidget extends Widget {

    private final ItemListGridWidget gridWidget;
    private final int index;

    ItemListSlotWidget(int x, int y, ItemListGridWidget gridWidget, int index) {
        super(new Position(x, y), new Size(18, 18));
        this.gridWidget = gridWidget;
        this.index = index;
    }

    public String formatItemAmount(int itemAmount) {
        return Integer.toString(itemAmount);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        GuiTextures.SLOT.draw(position.x, position.y, 18, 18);
        IItemInfo itemInfo = gridWidget.getItemInfoAt(index);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (itemInfo != null) {
            ItemStack itemStack = itemInfo.getItemStackKey().getItemStackRaw();
            String itemAmountStr = formatItemAmount(itemInfo.getTotalItemAmount());
            drawItemStack(itemStack, stackX, stackY, null);
            drawStringFixedCorner(itemAmountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY, 16, 16);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        IItemInfo itemInfo = gridWidget.getItemInfoAt(index);
        if (itemInfo != null && isMouseOverElement(mouseX, mouseY)) {
            ItemStack itemStack = itemInfo.getItemStackKey().getItemStackRaw();
            List<String> tooltip = getItemToolTip(itemStack);
            int totalItemStored = itemInfo.getTotalItemAmount();
            String itemStoredText = I18n.format("gregtech.item_list.item_stored", totalItemStored);
            tooltip.add(TextFormatting.GRAY + itemStoredText);
            drawHoveringText(itemStack, tooltip, -1, mouseX, mouseY);
        }
    }

    private void setCreativeHeldItem(ItemStack itemStack) {
        InventoryPlayer inventory = gui.entityPlayer.inventory;
        if (!itemStack.isEmpty() && inventory.getItemStack().isEmpty()) {
            itemStack.setCount(itemStack.getMaxStackSize());
            inventory.setItemStack(itemStack);
        }
    }

    private int getAmountToTake(ItemStack itemStack, int maxAmount, int button) {
        int maxStackSize = Math.min(itemStack.getMaxStackSize(), maxAmount);
        return button == 0 ? maxStackSize : (maxStackSize >= 2 ? maxStackSize / 2 : 1);
    }

    //returns true if something actually happened
    private boolean insertHeldItemStack(int button, boolean isClient) {
        InventoryPlayer inventory = gui.entityPlayer.inventory;
        int amountToInsert = button == 1 ? 1 : Integer.MAX_VALUE;
        if (!inventory.getItemStack().isEmpty()) {
            if (!isClient) {
                //on server, we lookup item list to see how much we can actually insert
                ItemStack heldItemStack = inventory.getItemStack();
                IItemList itemList = gridWidget.getItemList();
                int amountInserted = itemList.insertItem(KeySharedStack.getRegisteredStack(heldItemStack), Math.min(heldItemStack.getCount(), amountToInsert), false, InsertMode.LOWEST_PRIORITY);
                heldItemStack.shrink(amountInserted);
                uiAccess.sendHeldItemUpdate();
                gui.entityPlayer.openContainer.detectAndSendChanges();
                return amountInserted > 0;
            } else {
                //on client we assume we can insert full stack into the network
                inventory.getItemStack().shrink(amountToInsert);
                return true;
            }
        }
        return false;
    }

    private void extractItemStack(ItemStackKey itemStackKey, int amount, boolean isClient) {
        InventoryPlayer inventory = gui.entityPlayer.inventory;
        if (inventory.getItemStack().isEmpty()) {
            if (!isClient) {
                //on server, we try to extract from the network
                IItemList itemList = gridWidget.getItemList();
                int amountExtracted = itemList.extractItem(itemStackKey, amount, false);
                if (amountExtracted > 0) {
                    ItemStack resultStack = itemStackKey.getItemStack();
                    resultStack.setCount(amountExtracted);
                    inventory.setItemStack(resultStack);
                }
                uiAccess.sendHeldItemUpdate();
            } else {
                //on client we assume we can extract as much items as user wishes
                ItemStack itemStack = itemStackKey.getItemStack();
                itemStack.setCount(amount);
                inventory.setItemStack(itemStack);
            }
        }
    }

    private void handleMouseClick(@Nullable IItemInfo itemInfo, int button, boolean isClient) {
        if (button == 2) {
            if (itemInfo != null && gui.entityPlayer.isCreative()) {
                ItemStack itemStack = itemInfo.getItemStackKey().getItemStack();
                setCreativeHeldItem(itemStack);
            }
        } else if (button == 0 || button == 1) {
            if (insertHeldItemStack(button, isClient) ||
                    !gui.entityPlayer.inventory.getItemStack().isEmpty()) {
                return;
            }
            if (itemInfo != null) {
                ItemStack itemStack = itemInfo.getItemStackKey().getItemStack();
                int extractAmount = getAmountToTake(itemStack, itemInfo.getTotalItemAmount(), button);
                extractItemStack(itemInfo.getItemStackKey(), extractAmount, isClient);
            }
        }
    }

    private void handleSelfShiftClick(IItemInfo itemInfo) {
        ItemStack itemStack = itemInfo.getItemStackKey().getItemStack();
        itemStack.setCount(itemStack.getMaxStackSize());
        int currentStackSize = itemStack.getCount();
        uiAccess.attemptMergeStack(itemStack, true, true);
        int amountToExtract = Math.min(currentStackSize - itemStack.getCount(), itemInfo.getTotalItemAmount());
        if (amountToExtract > 0) {
            int extracted = gridWidget.getItemList().extractItem(itemInfo.getItemStackKey(), amountToExtract, false);
            ItemStack resultStack = itemInfo.getItemStackKey().getItemStack();
            resultStack.setCount(extracted);
            if (!resultStack.isEmpty()) {
                uiAccess.attemptMergeStack(resultStack, true, false);
                gui.entityPlayer.openContainer.detectAndSendChanges();
                if (!resultStack.isEmpty()) {
                    gui.entityPlayer.dropItem(resultStack, false, false);
                }
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            try {
                ItemStack itemStack = buffer.readItemStack();
                int button = buffer.readVarInt();
                IItemInfo itemInfo = itemStack.isEmpty() ? null : gridWidget.getItemList().getItemInfo(KeySharedStack.getRegisteredStack(itemStack));
                handleMouseClick(itemInfo, button, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (id == 2) {
            try {
                ItemStack itemStack = buffer.readItemStack();
                IItemInfo itemInfo = gridWidget.getItemList().getItemInfo(KeySharedStack.getRegisteredStack(itemStack));
                if (itemInfo != null) {
                    handleSelfShiftClick(itemInfo);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void dispatchMouseClick(int button) {
        IItemInfo itemInfo = gridWidget.getItemInfoAt(index);
        handleMouseClick(itemInfo, button, true);
        ItemStack itemStack = itemInfo == null ? ItemStack.EMPTY : itemInfo.getItemStackKey().getItemStack();
        writeClientAction(1, buf -> {
            buf.writeItemStack(itemStack);
            buf.writeVarInt(button);
        });
    }

    private void dispatchSelfShiftClick() {
        IItemInfo itemInfo = gridWidget.getItemInfoAt(index);
        if (itemInfo != null) {
            writeClientAction(2, buf -> buf.writeItemStack(itemInfo.getItemStackKey().getItemStackRaw()));
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            boolean shiftClick = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (!shiftClick) {
                dispatchMouseClick(button);
            } else {
                dispatchSelfShiftClick();
            }
            return true;
        }
        return false;
    }
}
