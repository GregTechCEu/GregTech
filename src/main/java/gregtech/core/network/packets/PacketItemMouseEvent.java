package gregtech.core.network.packets;

import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IMouseEventHandler;
import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PacketItemMouseEvent implements IPacket, IServerExecutor {

    private final ByteBuf internalBuffer = Unpooled.buffer();

    public @NotNull PacketBuffer getBuffer() {
        return new PacketBuffer(internalBuffer.asReadOnly());
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBytes(internalBuffer.array());
    }

    @Override
    public void decode(PacketBuffer buf) {
        internalBuffer.writeBytes(buf.array());
    }

    public static void toServer(@NotNull Consumer<@NotNull PacketBuffer> bufferWriter) {
        PacketItemMouseEvent packet = new PacketItemMouseEvent();
        bufferWriter.accept(new PacketBuffer(packet.internalBuffer));
        GregTechAPI.networkHandler.sendToServer(packet);
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.player;
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() instanceof MetaItem<?>metaItem) {
            IMouseEventHandler mouseEventHandler = metaItem.getMouseEventHandler(stack);
            if (mouseEventHandler != null) {
                mouseEventHandler.handleMouseEventServer(this, player, stack);
            }
        }
    }
}
