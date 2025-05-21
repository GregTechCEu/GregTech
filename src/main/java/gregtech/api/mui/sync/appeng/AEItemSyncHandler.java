package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import appeng.api.storage.data.IAEItemStack;

public class AEItemSyncHandler extends AESyncHandler<IAEItemStack> {

    public AEItemSyncHandler(IConfigurableSlot<IAEItemStack> config) {
        super(config);
        cache = new ExportOnlyAEItemSlot();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == configSyncID) {
            if (buf.readBoolean()) {
                config.setConfig(WrappedItemStack.fromPacket(buf));
            } else {
                config.setConfig(null);
            }
        } else if (id == stockSyncID) {
            if (buf.readBoolean()) {
                config.setStock(WrappedItemStack.fromPacket(buf));
            } else {
                config.setStock(null);
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        super.readOnServer(id, buf);
        if (id == setConfigID) {
            config.setConfig(WrappedItemStack.fromPacket(buf));
        }
    }

    public void setConfig(ItemStack stack) {
        syncToServer(setConfigID, buf -> ByteBufUtils.writeTag(buf, stack.serializeNBT()));
    }
}
