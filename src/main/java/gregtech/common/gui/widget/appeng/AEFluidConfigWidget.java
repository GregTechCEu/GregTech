package gregtech.common.gui.widget.appeng;

import gregtech.common.gui.widget.appeng.slot.AEFluidConfigSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEFluidStack;

/**
 * @Author GlodBlock
 * @Description Display {@link IAEFluidStack} config
 * @Date 2023/4/21-1:45
 */
public class AEFluidConfigWidget extends AEConfigWidget<IAEFluidStack> {

    public AEFluidConfigWidget(int x, int y, IConfigurableSlot<IAEFluidStack>[] config) {
        super(x, y, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new MetaTileEntityMEInputHatch.ExportOnlyAEFluid();
            this.cached[index] = new MetaTileEntityMEInputHatch.ExportOnlyAEFluid();
            line = index / 8;
            this.addWidget(new AEFluidConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == UPDATE_ID) {
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                int index = buffer.readVarInt();
                IConfigurableSlot<IAEFluidStack> slot = this.displayList[index];
                if (buffer.readBoolean()) {
                    slot.setConfig(WrappedFluidStack.fromPacket(buffer));
                } else {
                    slot.setConfig(null);
                }
                if (buffer.readBoolean()) {
                    slot.setStock(WrappedFluidStack.fromPacket(buffer));
                } else {
                    slot.setStock(null);
                }
            }
        }
    }
}
