package gregtech.common.gui.widget.appeng;

import gregtech.common.gui.widget.appeng.slot.AEItemConfigSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEItemStack;

/**
 * @Author GlodBlock
 * @Description Display {@link IAEItemStack} config
 * @Date 2023/4/22-1:02
 */
public class AEItemConfigWidget extends AEConfigWidget<IAEItemStack> {

    public AEItemConfigWidget(int x, int y, IConfigurableSlot<IAEItemStack>[] config) {
        super(x, y, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new MetaTileEntityMEInputBus.ExportOnlyAEItem();
            this.cached[index] = new MetaTileEntityMEInputBus.ExportOnlyAEItem();
            line = index / 8;
            this.addWidget(new AEItemConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
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
