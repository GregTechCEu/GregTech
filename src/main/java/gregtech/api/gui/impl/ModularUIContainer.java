package gregtech.api.gui.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetUIAccess;
import gregtech.api.util.GTUtility;
import gregtech.api.util.PerTickIntCounter;
import gregtech.core.network.packets.PacketUIClientAction;
import gregtech.core.network.packets.PacketUIWidgetUpdate;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketSetSlot;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModularUIContainer extends Container implements WidgetUIAccess {

    protected final HashMap<Slot, INativeWidget> slotMap = new HashMap<>();
    private final ModularUI modularUI;

    public boolean accumulateWidgetUpdateData = false;
    public final List<PacketUIWidgetUpdate> accumulatedUpdates = new ArrayList<>();

    public ModularUIContainer(ModularUI modularUI) {
        this.modularUI = modularUI;
        modularUI.guiWidgets.values().forEach(widget -> widget.setUiAccess(this));
        modularUI.guiWidgets.values().stream()
                .flatMap(widget -> widget.getNativeWidgets().stream())
                .forEach(nativeWidget -> {
                    Slot slot = nativeWidget.getHandle();
                    slotMap.put(slot, nativeWidget);
                    addSlotToContainer(slot);
                });
        modularUI.triggerOpenListeners();
    }

    @Override
    public void notifySizeChange() {}

    // WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    // OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    @Override
    public void notifyWidgetChange() {
        List<INativeWidget> nativeWidgets = modularUI.guiWidgets.values().stream()
                .flatMap(widget -> widget.getNativeWidgets().stream())
                .collect(Collectors.toList());

        Set<INativeWidget> removedWidgets = new HashSet<>(slotMap.values());
        nativeWidgets.forEach(removedWidgets::remove);
        if (!removedWidgets.isEmpty()) {
            for (INativeWidget removedWidget : removedWidgets) {
                Slot slotHandle = removedWidget.getHandle();
                this.slotMap.remove(slotHandle);
                // replace removed slot with empty placeholder to avoid list index shift
                EmptySlotPlaceholder emptySlotPlaceholder = new EmptySlotPlaceholder();
                emptySlotPlaceholder.slotNumber = slotHandle.slotNumber;
                this.inventorySlots.set(slotHandle.slotNumber, emptySlotPlaceholder);
                this.inventoryItemStacks.set(slotHandle.slotNumber, ItemStack.EMPTY);
            }
        }

        Set<INativeWidget> addedWidgets = new HashSet<>(nativeWidgets);
        addedWidgets.removeAll(slotMap.values());
        if (!addedWidgets.isEmpty()) {
            int[] emptySlotIndexes = inventorySlots.stream()
                    .filter(it -> it instanceof EmptySlotPlaceholder)
                    .mapToInt(slot -> slot.slotNumber).toArray();
            int currentIndex = 0;
            for (INativeWidget addedWidget : addedWidgets) {
                Slot slotHandle = addedWidget.getHandle();
                // add or replace empty slot in inventory
                this.slotMap.put(slotHandle, addedWidget);
                if (currentIndex < emptySlotIndexes.length) {
                    int slotIndex = emptySlotIndexes[currentIndex++];
                    slotHandle.slotNumber = slotIndex;
                    this.inventorySlots.set(slotIndex, slotHandle);
                    this.inventoryItemStacks.set(slotIndex, ItemStack.EMPTY);
                } else {
                    slotHandle.slotNumber = this.inventorySlots.size();
                    this.inventorySlots.add(slotHandle);
                    this.inventoryItemStacks.add(ItemStack.EMPTY);
                }
            }
        }
    }

    public ModularUI getModularUI() {
        return modularUI;
    }

    @Override
    public void onContainerClosed(@NotNull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        modularUI.triggerCloseListeners();
    }

    @Override
    public void addListener(@NotNull IContainerListener listener) {
        super.addListener(listener);
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void sendSlotUpdate(INativeWidget slot) {
        Slot slotHandle = slot.getHandle();
        for (IContainerListener listener : listeners) {
            listener.sendSlotContents(this, slotHandle.slotNumber, slotHandle.getStack());
        }
    }

    @Override
    public void sendHeldItemUpdate() {
        for (IContainerListener listener : listeners) {
            if (listener instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) listener;
                player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (listeners.size() > 0) {
            modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
        }
    }

    @NotNull
    @Override
    public ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        if (slotId >= 0 && slotId < inventorySlots.size()) {
            Slot slot = getSlot(slotId);
            ItemStack result = slotMap.get(slot).slotClick(dragType, clickTypeIn, player);
            if (result == null) {
                return super.slotClick(slotId, dragType, clickTypeIn, player);
            }
            return result;
        }
        if (slotId == -999) {
            super.slotClick(slotId, dragType, clickTypeIn, player);
        }
        return ItemStack.EMPTY;
    }

    private final PerTickIntCounter transferredPerTick = new PerTickIntCounter(0);

    private List<INativeWidget> getShiftClickSlots(ItemStack itemStack, boolean fromContainer) {
        return slotMap.values().stream()
                .filter(it -> it.canMergeSlot(itemStack))
                .filter(it -> it.getSlotLocationInfo().isPlayerInventory == fromContainer)
                .filter(it -> {
                    if (it.getHandle() instanceof SlotWidget.WidgetSlotItemHandler) {
                        return ((SlotWidget.WidgetSlotItemHandler) it.getHandle()).canShiftClickInto();
                    }
                    return true;
                })
                .sorted(Comparator.comparing(s -> (fromContainer ? -1 : 1) * s.getHandle().slotNumber))
                .collect(Collectors.toList());
    }

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate) {
        List<Slot> inventorySlots = getShiftClickSlots(itemStack, fromContainer).stream()
                .map(INativeWidget::getHandle)
                .collect(Collectors.toList());
        return GTUtility.mergeItemStack(itemStack, inventorySlots, simulate);
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (!slot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }
        if (!slot.getHasStack()) {
            // return empty if we can't transfer it
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = slot.getStack();
        ItemStack stackToMerge = slotMap.get(slot).onItemTake(player, stackInSlot.copy(), true);
        boolean fromContainer = !slotMap.get(slot).getSlotLocationInfo().isPlayerInventory;
        if (!attemptMergeStack(stackToMerge, fromContainer, true)) {
            return ItemStack.EMPTY;
        }
        int itemsMerged;
        if (stackToMerge.isEmpty() || slotMap.get(slot).canMergeSlot(stackToMerge)) {
            itemsMerged = stackInSlot.getCount() - stackToMerge.getCount();
        } else {
            // if we can't have partial stack merge, we have to use all the stack
            itemsMerged = stackInSlot.getCount();
        }
        int itemsToExtract = itemsMerged;
        itemsMerged += transferredPerTick.get(player.world);
        if (itemsMerged > stackInSlot.getMaxStackSize()) {
            // we can merge at most one stack at a time
            return ItemStack.EMPTY;
        }
        transferredPerTick.increment(player.world, itemsToExtract);
        // otherwise, perform extraction and merge
        ItemStack extractedStack = stackInSlot.splitStack(itemsToExtract);
        if (stackInSlot.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        extractedStack = slotMap.get(slot).onItemTake(player, extractedStack, false);
        ItemStack resultStack = extractedStack.copy();
        if (!attemptMergeStack(extractedStack, fromContainer, false)) {
            resultStack = ItemStack.EMPTY;
        }
        if (!extractedStack.isEmpty()) {
            player.dropItem(extractedStack, false, false);
            resultStack = ItemStack.EMPTY;
        }
        return resultStack;
    }

    @Override
    public boolean canMergeSlot(@NotNull ItemStack stack, @NotNull Slot slotIn) {
        return slotMap.get(slotIn).canMergeSlot(stack);
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return playerIn == this.modularUI.entityPlayer && this.modularUI.holder.isValid();
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        if (modularUI.entityPlayer instanceof EntityPlayerSP) {
            GregTechAPI.networkHandler.sendToServer(new PacketUIClientAction(windowId, widgetId, packetBuffer));
        }
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        if (modularUI.entityPlayer instanceof EntityPlayerMP) {
            PacketUIWidgetUpdate widgetUpdate = new PacketUIWidgetUpdate(windowId, widgetId, packetBuffer);
            if (!accumulateWidgetUpdateData) {
                GregTechAPI.networkHandler.sendTo(widgetUpdate, (EntityPlayerMP) modularUI.entityPlayer);
            } else {
                accumulatedUpdates.add(widgetUpdate);
            }
        }
    }

    private static class EmptySlotPlaceholder extends Slot {

        private static final IInventory EMPTY_INVENTORY = new InventoryBasic("Empty Inventory", false, 0);

        public EmptySlotPlaceholder() {
            super(EMPTY_INVENTORY, 0, -100000, -100000);
        }

        @NotNull
        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {}

        @Override
        public boolean isItemValid(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
