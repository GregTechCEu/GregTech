package gregtech.common.gui.widget.appeng;

import gregtech.common.gui.widget.appeng.slot.AEFluidConfigSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;

public class AEFluidConfigWidget extends AEConfigWidget<IAEFluidStack> {

    final ExportOnlyAEFluidList fluidList;

    public AEFluidConfigWidget(int x, int y, ExportOnlyAEFluidList fluidList) {
        super(x, y, fluidList.getInventory(), fluidList.isStocking());
        this.fluidList = fluidList;
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEFluidSlot();
            this.cached[index] = new ExportOnlyAEFluidSlot();
            line = index / 8;
            this.addWidget(new AEFluidConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }

    public boolean hasStackInConfig(FluidStack stack) {
        return fluidList.hasStackInConfig(stack, true);
    }

    public boolean isAutoPull() {
        return fluidList.isAutoPull();
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
