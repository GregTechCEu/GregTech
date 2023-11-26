package gregtech.api.gui.impl;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetUIAccess;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;

import com.google.common.collect.Lists;

import java.util.List;

public abstract class FakeModularGuiContainer implements WidgetUIAccess {

    protected final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    public final List<Slot> inventorySlots = Lists.newArrayList();
    public final ModularUI modularUI;
    protected int windowId;

    public FakeModularGuiContainer(ModularUI modularUI) {
        this.modularUI = modularUI;
        modularUI.initWidgets();
        modularUI.guiWidgets.values().forEach(widget -> widget.setUiAccess(this));
        modularUI.guiWidgets.values().stream().flatMap(widget -> widget.getNativeWidgets().stream())
                .forEach(nativeWidget -> addSlotToContainer(nativeWidget.getHandle()));
        modularUI.triggerOpenListeners();
    }

    protected void addSlotToContainer(Slot slotIn) {
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
    }

    public void handleSlotUpdate(PacketBuffer updateData) {
        try {
            int size = updateData.readVarInt();
            for (int i = 0; i < size; i++) {
                inventorySlots.get(updateData.readVarInt()).putStack(updateData.readItemStack());
            }
        } catch (Exception ignored) {

        }
    }

    public void handleClientAction(PacketBuffer buffer) {
        if (detectSyncedPacket(buffer)) {
            Widget widget = modularUI.guiWidgets.get(buffer.readVarInt());
            if (widget != null) {
                widget.handleClientAction(buffer.readVarInt(), buffer);
            }
        }
    }

    // Detects if a client action is germane to a container. THIS MAY MODIFY THE CONTAINER.
    public abstract boolean detectSyncedPacket(PacketBuffer buffer);

    public abstract void detectAndSendChanges();

    @Override
    public void notifySizeChange() {}

    @Override
    public void notifyWidgetChange() {}

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean b, boolean b1) {
        return false;
    }

    @Override
    public void sendSlotUpdate(INativeWidget iNativeWidget) {}

    @Override
    public void sendHeldItemUpdate() {}
}
