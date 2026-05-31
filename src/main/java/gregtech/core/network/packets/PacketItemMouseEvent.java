package gregtech.core.network.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.stats.IMouseEventHandler;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PacketItemMouseEvent implements IPacket, IServerExecutor {

    @NotNull
    private final ByteBuf internalBuffer;

    public PacketItemMouseEvent() {
        this.internalBuffer = Unpooled.buffer();
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBytes(internalBuffer);
    }

    @Override
    public void decode(PacketBuffer buf) {
        internalBuffer.writeBytes(buf);
    }

    public static void toServer(@NotNull Consumer<@NotNull PacketBuffer> bufferWriter, @NotNull EnumHand hand) {
        PacketItemMouseEvent packet = new PacketItemMouseEvent();
        PacketBuffer buf = new PacketBuffer(packet.internalBuffer);
        buf.writeByte(hand.ordinal());
        bufferWriter.accept(buf);

        GregTechAPI.networkHandler.sendToServer(packet);
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.player;
        EnumHand hand = EnumHand.values()[internalBuffer.readByte()];
        ItemStack stack = player.getHeldItem(hand);

        IMouseEventHandler mouseEventHandler = IMouseEventHandler.getHandler(stack);
        if (mouseEventHandler != null) {
            mouseEventHandler.handleMouseEventServer(new PacketBuffer(internalBuffer.asReadOnly()), player, hand,
                    stack);
        }
    }
}
