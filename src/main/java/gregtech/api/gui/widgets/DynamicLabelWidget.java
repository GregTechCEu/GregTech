package gregtech.api.gui.widgets;

import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

/**
 * Represents a label with text, dynamically obtained
 * from supplied getter in constructor
 * Note that this DOESN'T DO SYNC and calls getter on client side only
 * if you're looking for server-side controlled text field, see {@link gregtech.api.gui.widgets.AdvancedTextWidget}
 */
public class DynamicLabelWidget extends Widget {

    protected final Supplier<String> textSupplier;
    private String lastTextValue = "";
    private final int color;

    public DynamicLabelWidget(int xPosition, int yPosition, Supplier<String> text) {
        this(xPosition, yPosition, text, 0x404040);
    }

    public DynamicLabelWidget(int xPosition, int yPosition, Supplier<String> text, int color) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.textSupplier = text;
        this.color = color;
    }

    @SideOnly(Side.CLIENT)
    private void updateSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String resultText = lastTextValue;
        setSize(new Size(fontRenderer.getStringWidth(resultText), fontRenderer.FONT_HEIGHT));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        String suppliedText = textSupplier.get();
        if (!suppliedText.equals(lastTextValue)) {
            this.lastTextValue = suppliedText;
            updateSize();
        }
        String[] split = textSupplier.get().split("\n");
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = getPosition();
        for (int i = 0; i < split.length; i++) {
            fontRenderer.drawString(split[i], position.x, position.y + (i * (fontRenderer.FONT_HEIGHT + 2)), color);
        }
    }
}
