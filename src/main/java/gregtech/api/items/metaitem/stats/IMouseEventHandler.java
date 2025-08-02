package gregtech.api.items.metaitem.stats;

import gregtech.core.network.packets.PacketItemMouseEvent;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

// TODO: add EnumHand to the methods/support for offhand items
/**
 * Implement on your {@link IItemComponent} to handle mouse events while the corresponding item is selected on the main
 * hotbar. <br/>
 * {@link MouseEvent#getX()}: The absolute position of the cursor on the X axis <br/>
 * {@link MouseEvent#getY()}: The absolute position of the cursor on the Y axis <br/>
 * {@link MouseEvent#getDx()}: The delta of cursor movement on the X axis <br/>
 * {@link MouseEvent#getDy()}: The delta of cursor movement on the Y axis <br/>
 * {@link MouseEvent#getDwheel()}: The scroll wheel delta: <br/>
 * {@code -120} = scrolling down <br/>
 * {@code 0} = no scrolling <br/>
 * {@code 120} = scrolling up <br/>
 * {@link MouseEvent#getButton()}: Which mouse button is being reported <br/>
 * {@code -1} = no click <br/>
 * {@code 0} = left click <br/>
 * {@code 1} = right click <br/>
 * {@code 2} = middle click <br/>
 * {@link MouseEvent#isButtonstate()}: If the reported mouse button has been pressed down or released: <br/>
 * {@code true} = pressed <br/>
 * {@code false} = unpressed
 */
public interface IMouseEventHandler extends IItemComponent {

    /**
     * Handle a mouse event on the client side. <br/>
     * Try to only act on mouse clicks or scrolls as looking around spams this event.
     * 
     * @param event        the event
     * @param playerClient the player object on the client side
     * @param stack        the {@link ItemStack} the player is holding in their main hand
     */
    @SideOnly(Side.CLIENT)
    void handleMouseEventClient(@NotNull MouseEvent event, @NotNull EntityPlayerSP playerClient,
                                @NotNull ItemStack stack);

    default void sendToServer(@NotNull Consumer<@NotNull PacketBuffer> bufferWriter) {
        PacketItemMouseEvent.toServer(bufferWriter);
    }

    /**
     * Handle the received mouse event on the server side.
     *
     * @param packet       the packet containing the data from the client event
     * @param playerServer the server side counterpart of the client player
     * @param stack        the stack the player was holding upon receiving the packet
     */
    void handleMouseEventServer(@NotNull PacketItemMouseEvent packet, @NotNull EntityPlayerMP playerServer,
                                @NotNull ItemStack stack);
}
