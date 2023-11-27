package gregtech.common.gui.impl;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.FakeModularGuiContainer;
import gregtech.common.items.behaviors.monitorplugin.FakeGuiPluginBehavior;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FakeModularUIPluginContainer extends FakeModularGuiContainer {

    protected int windowId;
    private final FakeGuiPluginBehavior behavior;
    public int syncId;

    public FakeModularUIPluginContainer(ModularUI modularUI, FakeGuiPluginBehavior pluginBehavior) {
        super(modularUI);
        this.behavior = pluginBehavior;
    }

    @Override
    public boolean detectSyncedPacket(PacketBuffer buffer) {
        int syncId = buffer.readVarInt();
        int windowId = buffer.readVarInt();
        return (syncId == this.syncId && windowId == this.windowId);
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
        if (toUpdate.size() > 0 && this.behavior != null) {
            behavior.writePluginData(GregtechDataCodes.UPDATE_FAKE_GUI_DETECT, packetBuffer -> {
                packetBuffer.writeVarInt(toUpdate.size());
                for (Tuple<Integer, ItemStack> tuple : toUpdate) {
                    packetBuffer.writeVarInt(tuple.getFirst());
                    packetBuffer.writeItemStack(tuple.getSecond());
                }
            });
        }
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        if (behavior != null) {
            behavior.writePluginAction(GregtechDataCodes.ACTION_FAKE_GUI, buffer -> {
                buffer.writeVarInt(syncId);
                buffer.writeVarInt(windowId);
                buffer.writeVarInt(modularUI.guiWidgets.inverse().get(widget));
                buffer.writeVarInt(updateId);
                payloadWriter.accept(buffer);
            });
        }
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        if (behavior != null) {
            behavior.writePluginData(GregtechDataCodes.UPDATE_FAKE_GUI, buf -> {
                buf.writeVarInt(windowId);
                buf.writeVarInt(modularUI.guiWidgets.inverse().get(widget));
                buf.writeVarInt(updateId);
                payloadWriter.accept(buf);
            });
        }
    }
}
