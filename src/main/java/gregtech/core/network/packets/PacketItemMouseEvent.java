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
import net.minecraftforge.client.event.MouseEvent;

import org.jetbrains.annotations.NotNull;

public class PacketItemMouseEvent implements IPacket, IServerExecutor {

    private int x;
    private int y;
    private int dx;
    private int dy;
    private int dwheel;
    private int button;
    private boolean buttonstate;

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeVarInt(dx);
        buf.writeVarInt(dy);
        buf.writeVarInt(dwheel);
        buf.writeVarInt(button);
        buf.writeBoolean(buttonstate);
    }

    @Override
    public void decode(PacketBuffer buf) {
        x = buf.readVarInt();
        y = buf.readVarInt();
        dx = buf.readVarInt();
        dy = buf.readVarInt();
        dwheel = buf.readVarInt();
        button = buf.readVarInt();
        buttonstate = buf.readBoolean();
    }

    public static void toServer(@NotNull MouseEvent event) {
        PacketItemMouseEvent packet = new PacketItemMouseEvent();
        packet.x = event.getX();
        packet.y = event.getY();
        packet.dx = event.getDx();
        packet.dy = event.getDy();
        packet.dwheel = event.getDwheel();
        packet.button = event.getButton();
        packet.buttonstate = event.isButtonstate();
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDwheel() {
        return dwheel;
    }

    public int getButton() {
        return button;
    }

    public boolean buttonState() {
        return buttonstate;
    }

    @Override
    public String toString() {
        return String.format("X: %d, Y: %d, Dx: %d, Dy: %d, DWheel: %d, Button: %d, Button State: %b",
                x, y, dx, dy, dwheel, button, buttonstate);
    }
}
