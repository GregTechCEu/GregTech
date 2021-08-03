package gregtech.api.gui.impl.fakegui;

import com.google.common.collect.Lists;
import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetUIAccess;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FakeModularUIContainer implements WidgetUIAccess {
    private final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    public final List<Slot> inventorySlots = Lists.newArrayList();
    public final ModularUI modularUI;
    protected int windowId;
    //private final FakeGuiPluginBehavior behavior;
    public int syncId;

    public FakeModularUIContainer(ModularUI modularUI/*, FakeGuiPluginBehavior pluginBehavior*/) {
        this.modularUI = modularUI;
        //this.behavior = pluginBehavior;
        modularUI.initWidgets();
        modularUI.guiWidgets.values().forEach(widget -> widget.setUiAccess(this));
        modularUI.guiWidgets.values().stream().flatMap(widget -> widget.getNativeWidgets().stream()).forEach(nativeWidget -> addSlotToContainer(nativeWidget.getHandle()));
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
        } catch (Exception ignored){

        }
    }

    public void handleClientAction(PacketBuffer buffer) {
        int syncId = buffer.readVarInt();
        int windowId = buffer.readVarInt();
        if (syncId == this.syncId && windowId == this.windowId) {
            Widget widget = modularUI.guiWidgets.get(buffer.readVarInt());
            if (widget != null) {
                widget.handleClientAction(buffer.readVarInt(), buffer);
            }
        }
        this.syncId = 0;
    }

    public void detectAndSendChanges() {
        List<Tuple<Integer, ItemStack>> toUpdate = new ArrayList<>();
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack real = this.inventorySlots.get(i).getStack();
            ItemStack fake = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(fake, real)) {
                boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(fake, real);
                fake = real.isEmpty() ? ItemStack.EMPTY : real.copy();
                this.inventoryItemStacks.set(i, fake);

                if (clientStackChanged) {
                    toUpdate.add(new Tuple<>(i, fake));
                }
            }
        }
        /*
        if (toUpdate.size() > 0 && this.behavior != null) {
            behavior.writePluginData(-1, packetBuffer -> {
                packetBuffer.writeVarInt(toUpdate.size());
                for (Tuple<Integer, ItemStack> tuple : toUpdate) {
                    packetBuffer.writeVarInt(tuple.getFirst());
                    packetBuffer.writeItemStack(tuple.getSecond());
                }
            });
        }
         */
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void notifySizeChange() {

    }

    @Override
    public void notifyWidgetChange() {

    }

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean b, boolean b1) {
        return false;
    }

    @Override
    public void sendSlotUpdate(INativeWidget iNativeWidget) {
    }

    @Override
    public void sendHeldItemUpdate() {
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        /*
        if (behavior != null) {
            behavior.writePluginAction(1, buffer -> {
                buffer.writeVarInt(syncId);
                buffer.writeVarInt(windowId);
                buffer.writeVarInt(modularUI.guiWidgets.inverse().get(widget));
                buffer.writeVarInt(updateId);
                payloadWriter.accept(buffer);
            });
        }
         */
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        /*
        if(behavior != null) {
            behavior.writePluginData(0, buf -> {
                buf.writeVarInt(windowId);
                buf.writeVarInt(modularUI.guiWidgets.inverse().get(widget));
                buf.writeVarInt(updateId);
                payloadWriter.accept(buf);
            });
        }
         */
    }
}
