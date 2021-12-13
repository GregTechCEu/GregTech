package gregtech.api.util;

import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.SPacketClipboard;
import net.minecraft.entity.player.EntityPlayerMP;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {

    public static void copyToClipboard(final String text) {
        if (Desktop.isDesktopSupported()) {
            final StringSelection selection = new StringSelection(text);
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        }
    }

    public static void copyToClipboard(final EntityPlayerMP player, final String text) {
        SPacketClipboard packet = new SPacketClipboard(text);
        NetworkHandler.channel.sendTo(packet.toFMLPacket(), player);
    }
}
