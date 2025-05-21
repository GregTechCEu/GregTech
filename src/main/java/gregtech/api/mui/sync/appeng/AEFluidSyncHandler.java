package gregtech.api.mui.sync.appeng;

import gregtech.api.util.NetworkUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;

import java.io.IOException;

public class AEFluidSyncHandler extends AESyncHandler<IAEFluidStack> {

    public AEFluidSyncHandler(IConfigurableSlot<IAEFluidStack> config) {
        super(config);
        cache = new ExportOnlyAEFluidSlot();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == configSyncID) {
            if (buf.readBoolean()) {
                config.setConfig(WrappedFluidStack.fromPacket(buf));
            } else {
                config.setConfig(null);
            }
        } else if (id == stockSyncID) {
            if (buf.readBoolean()) {
                config.setStock(WrappedFluidStack.fromPacket(buf));
            } else {
                config.setConfig(null);
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        super.readOnServer(id, buf);
        if (id == setConfigID) {
            config.setConfig(WrappedFluidStack.fromPacket(buf));
        }
    }

    public void setConfig(FluidStack stack) {
        syncToServer(setConfigID, buf -> NetworkUtil.writeFluidStack(buf, stack));
    }
}
