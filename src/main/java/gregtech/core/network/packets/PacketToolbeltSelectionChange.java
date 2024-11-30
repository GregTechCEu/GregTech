package gregtech.core.network.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class PacketToolbeltSelectionChange implements IPacket {

    protected int slot;

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

    public static void toClient(int slot, int matrixSlot, EntityPlayerMP player) {
        GregTechAPI.networkHandler.sendTo(new Client(slot, matrixSlot), player);
    }

    public static void toServer(int slot) {
        GregTechAPI.networkHandler.sendToServer(new Server(slot));
    }

    public static class Server extends PacketToolbeltSelectionChange implements IServerExecutor {

        public Server() {}

        public Server(int slot) {
            super(slot);
        }

        @Override
        public void executeServer(NetHandlerPlayServer handler) {
            EntityPlayerMP player = handler.player;
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemGTToolbelt toolbelt) {
                player.getServerWorld().playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.CLICK,
                        SoundCategory.PLAYERS, 2F, 1F);
                toolbelt.setSelectedTool(slot, stack);
            }
        }
    }

    public static class Client extends PacketToolbeltSelectionChange implements IClientExecutor {

        int index;

        public Client() {}

        public Client(int slot, int index) {
            super(slot);
            this.index = index;
        }

        @Override
        public void encode(PacketBuffer buf) {
            super.encode(buf);
            buf.writeVarInt(index);
        }

        @Override
        public void decode(PacketBuffer buf) {
            super.decode(buf);
            this.index = buf.readVarInt();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void executeClient(NetHandlerPlayClient handler) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            ItemStack stack = player.openContainer.getSlot(index).getStack();
            if (stack.getItem() instanceof ItemGTToolbelt toolbelt)
                toolbelt.setSelectedTool(slot, stack);
        }
    }
}
