package gregtech.core.network.packets;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;

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
        EntityPlayerMP player = handler.player;
        ItemStack stack = player.getHeldItemMainhand();
        Item item = stack.getItem();
        if (item instanceof ItemGTToolbelt toolbelt) {
            player.getServerWorld().playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.CLICK,
                    SoundCategory.PLAYERS, 2F, 1F);
            toolbelt.setSelectedTool(slot, stack);
        }
    }
}
