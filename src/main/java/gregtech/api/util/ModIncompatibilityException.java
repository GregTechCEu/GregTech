package gregtech.api.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ModIncompatibilityException extends CustomModLoadingErrorDisplayException {

    @SuppressWarnings("all")
    private static final long serialVersionUID = 1L;

    private final List<String> messages;

    public ModIncompatibilityException(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public void initGui(GuiErrorScreen guiErrorScreen, FontRenderer fontRenderer) {}

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseX, int mouseY, float time) {
        int x = errorScreen.width / 2;
        int y = 75;
        for (String message : messages) {
            errorScreen.drawCenteredString(fontRenderer, message, x, y, 0xFFFFFF);
            y += 15;
        }
    }
}
