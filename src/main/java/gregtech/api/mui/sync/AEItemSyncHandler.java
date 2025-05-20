package gregtech.api.mui.sync;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import appeng.api.storage.data.IAEItemStack;

import java.io.IOException;

public class AEItemSyncHandler extends AESyncHandler<IAEItemStack> {

    public AEItemSyncHandler(IConfigurableSlot<IAEItemStack> config) {
        super(config);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == configSyncID) {
            if (buf.readBoolean()) {
                config.setConfig(WrappedItemStack.fromPacket(buf));
            } else {
                config.setConfig(null);
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == jeiDropSyncID) {
            config.setConfig(WrappedItemStack.fromPacket(buf));
        }
    }

    public void sendJEIDrop(ItemStack stack) {
        syncToServer(jeiDropSyncID, buf -> ByteBufUtils.writeTag(buf, stack.serializeNBT()));
    }
}
