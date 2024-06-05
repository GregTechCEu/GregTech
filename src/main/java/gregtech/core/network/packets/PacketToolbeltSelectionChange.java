package gregtech.core.network.packets;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

public class PacketToolbeltSelectionChange implements IPacket, IServerExecutor {

    private int slot;

    public PacketToolbeltSelectionChange() {}

    public PacketToolbeltSelectionChange(int slot) {
        this.slot = slot;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeByte(slot);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.slot = buf.readByte();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        ItemStack stack = handler.player.getHeldItemMainhand();
        Item item = stack.getItem();
        if (item instanceof ItemGTToolbelt toolbelt) {
            toolbelt.setSelectedTool(slot, stack);
        }
    }
}
