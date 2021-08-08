package gregtech.api.gui.widgets;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

/**
 * Provides access to UI's syncing mechanism, allowing to send arbitrary data and
 * receive it on the client or server side via callbacks provided to widgets
 * IDs are used only by your own widget, use IDs you like
 */
public interface WidgetUIAccess {

    void notifySizeChange();

    /**
     * Call when widget is added/removed, or INativeWidget list changed
     * and should be updated accordingly
     */
    void notifyWidgetChange();

    /**
     * Attempts to perform a slot merging (shift-click) for a given stack
     * with the slots either from player inventory, or a container
     *
     * @return true if stack was merged
     */
    boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate);

    /**
     * Sends force slot update to the client, even if stack didn't change from the server perspective
     * Can be useful if client and server handle stack removal differently via override of Slot#onTake to
     * notify client of stack status computed on server side
     * Used for crafting table result stack taking, for example
     */
    void sendSlotUpdate(INativeWidget slot);

    /**
     * Sends update of the item held on the mouse by the viewer player to the client
     * Useful for sending item update without re-syncing entire inventory via player.sendAlLContents
     * Used for retrieving items server-side, for example
     */
    void sendHeldItemUpdate();

    /**
     * Sends action to the server with the ID and data payload supplied
     * Server will receive it in {@link Widget#handleClientAction(int, PacketBuffer)}
     */
    void writeClientAction(Widget widget, int id, Consumer<PacketBuffer> payloadWriter);

    /**
     * Sends update to the client from the server
     * Usually called when internal state changes in {@link Widget#detectAndSendChanges()}
     * Client will receive payload data in {@link Widget#readUpdateInfo(int, PacketBuffer)}
     */
    void writeUpdateInfo(Widget widget, int id, Consumer<PacketBuffer> payloadWriter);

}
