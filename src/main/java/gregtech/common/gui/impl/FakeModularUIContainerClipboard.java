package gregtech.common.gui.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.FakeModularGuiContainer;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import gregtech.core.network.packets.PacketClipboardUIWidgetUpdate;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_UI;

public class FakeModularUIContainerClipboard extends FakeModularGuiContainer {

    private final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    public final List<Slot> inventorySlots = Lists.newArrayList();
    public int windowId;
    public MetaTileEntityClipboard clipboard;

    public FakeModularUIContainerClipboard(ModularUI modularUI, MetaTileEntityClipboard clipboard) {
        super(modularUI);
        this.clipboard = clipboard;
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
        int windowId = buffer.readVarInt();
        if (windowId == this.windowId) {
            Widget widget = modularUI.guiWidgets.get(buffer.readVarInt());
            if (widget != null) {
                widget.handleClientAction(buffer.readVarInt(), buffer);
            }
        }
    }

    @Override
    public boolean detectSyncedPacket(PacketBuffer buffer) {
        return this.windowId == buffer.readVarInt();
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
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeVarInt(windowId);
        packetBuffer.writeVarInt(widgetId);
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        GregTechAPI.networkHandler.sendToServer(new PacketClipboardUIWidgetUpdate(
                this.clipboard.getWorld().provider.getDimension(),
                this.clipboard.getPos(),
                updateId, packetBuffer));
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        this.clipboard.writeCustomData(UPDATE_UI + modularUI.guiWidgets.inverse().get(widget), buf -> {
            buf.writeVarInt(windowId);
            buf.writeVarInt(modularUI.guiWidgets.inverse().get(widget));
            buf.writeVarInt(updateId);
            payloadWriter.accept(buf);
        });
    }
}
