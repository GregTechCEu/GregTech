package gregtech.common.gui.widget.appeng;

import gregtech.common.gui.widget.appeng.slot.AEItemConfigSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEItemStack;

import java.util.function.Supplier;

public class AEItemConfigWidget extends AEConfigWidget<IAEItemStack> {

    final Supplier<Boolean> autoPull;

    public AEItemConfigWidget(int x, int y, IConfigurableSlot<IAEItemStack>[] config, boolean isStocking,
                              Supplier<Boolean> autoPull) {
        super(x, y, config, isStocking);
        this.autoPull = autoPull;
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEItemSlot();
            this.cached[index] = new ExportOnlyAEItemSlot();
            line = index / 8;
            this.addWidget(new AEItemConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }

    public boolean hasStackInConfig(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (IConfigurableSlot<IAEItemStack> slot : this.displayList) {
            IAEItemStack config = slot.getConfig();
            if (config != null && config.isSameType(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAutoPull() {
        return autoPull.get();
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
