package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
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
        cache = new ExportOnlyAEItemSlot();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == configSyncID) {
            if (onConfigChanged != null) {
                onConfigChanged.run();
            }

            if (buf.readBoolean()) {
                config.setConfig(WrappedItemStack.fromPacket(buf));
            } else {
                config.setConfig(null);
            }
        } else if (id == stockSyncID) {
            if (onStockChanged != null) {
                onStockChanged.run();
            }

            if (buf.readBoolean()) {
                config.setStock(WrappedItemStack.fromPacket(buf));
            } else {
                config.setStock(null);
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
