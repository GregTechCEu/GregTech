package gregtech.api.items.metaitem.stats;

import gregtech.core.network.packets.PacketItemMouseEvent;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Implement on your {@link IItemComponent} to handle mouse event while the corresponding item is selected on the main
 * hotbar.
 */
public interface IMouseEventHandler extends IItemComponent {

    /**
     * Handle a mouse event on the client side. <br/>
     * Use {@link PacketItemMouseEvent#toServer(MouseEvent)} to send the event to the server. <br/>
     * Try to only act on mouse clicks or scrolls as looking around spams this event.
     * 
     * @param event        the event
     * @param playerClient the player object on the client side
     * @param stack        the {@link ItemStack} the player is holding in their main hand
     */
    @SideOnly(Side.CLIENT)
    void handleMouseEventClient(@NotNull MouseEvent event, @NotNull EntityPlayerSP playerClient,
                                @NotNull ItemStack stack);

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
