package gregtech.api.util;

import gregtech.api.GregTechAPI;
import gregtech.core.network.packets.PacketClipboard;

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
        GregTechAPI.networkHandler.sendTo(new PacketClipboard(text), player);
    }
}
