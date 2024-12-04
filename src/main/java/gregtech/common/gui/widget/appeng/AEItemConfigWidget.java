package gregtech.common.gui.widget.appeng;

import gregtech.common.gui.widget.appeng.slot.AEItemConfigSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEItemStack;

public class AEItemConfigWidget extends AEConfigWidget<IAEItemStack> {

    final ExportOnlyAEItemList itemList;

    public AEItemConfigWidget(int x, int y, ExportOnlyAEItemList itemList) {
        super(x, y, itemList.getInventory(), itemList.isStocking());
        this.itemList = itemList;
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        final int size = (int) Math.sqrt(this.config.length);
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int h = 0; h < size; h++) {
            for (int w = 0; w < size; w++) {
                final int index = h * size + w;
                this.displayList[index] = new ExportOnlyAEItemSlot();
                this.cached[index] = new ExportOnlyAEItemSlot();
                this.addWidget(new AEItemConfigSlot(w * 18, h * 18, this, index));
            }
        }
    }

    public boolean hasStackInConfig(ItemStack stack) {
        return itemList.hasStackInConfig(stack, true);
    }

    public boolean isAutoPull() {
        return itemList.isAutoPull();
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == UPDATE_ID) {
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                int index = buffer.readVarInt();
                IConfigurableSlot<IAEItemStack> slot = this.displayList[index];
                if (buffer.readBoolean()) {
                    slot.setConfig(WrappedItemStack.fromPacket(buffer));
                } else {
                    slot.setConfig(null);
                }
                if (buffer.readBoolean()) {
                    slot.setStock(WrappedItemStack.fromPacket(buffer));
                } else {
                    slot.setStock(null);
                }
            }
        }
    }
}
