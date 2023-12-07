package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Simple one-line text widget with text synced and displayed
 * as the raw string from the server
 */
public class SimpleTextWidget extends Widget {

    protected final String formatLocale;
    protected final int color;
    protected final Supplier<String> textSupplier;
    protected String lastText = "";
    protected boolean isCentered = true;
    protected boolean clientWidget;
    protected boolean isShadow;
    protected float scale = 1;
    protected int width;

    public SimpleTextWidget(int xPosition, int yPosition, String formatLocale, int color,
                            Supplier<String> textSupplier) {
        this(xPosition, yPosition, formatLocale, color, textSupplier, false);
    }

    public SimpleTextWidget(int xPosition, int yPosition, String formatLocale, int color, Supplier<String> textSupplier,
                            boolean clientWidget) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.color = color;
        this.formatLocale = formatLocale;
        this.textSupplier = textSupplier;
        this.clientWidget = clientWidget;
    }

    public SimpleTextWidget setWidth(int width) {
        this.width = width;
        return this;
    }

    public SimpleTextWidget(int xPosition, int yPosition, String formatLocale, Supplier<String> textSupplier) {
        this(xPosition, yPosition, formatLocale, 0x404040, textSupplier, false);
    }

    public SimpleTextWidget setShadow(boolean shadow) {
        isShadow = shadow;
        return this;
    }

    public SimpleTextWidget setCenter(boolean isCentered) {
        this.isCentered = isCentered;
        return this;
    }

    public SimpleTextWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    private void updateSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(lastText);
        setSize(new Size(stringWidth, fontRenderer.FONT_HEIGHT));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (clientWidget && textSupplier != null) {
            String newString = textSupplier.get();
            if (!newString.equals(lastText)) {
                lastText = newString;
                updateSize();
            }
            lastText = newString;
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        String text = formatLocale.isEmpty() ? (I18n.hasKey(lastText) ? I18n.format(lastText) : lastText) :
                I18n.format(formatLocale, lastText);
        List<String> texts;
        if (this.width > 0) {
            texts = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, (int) (width * (1 / scale)));
        } else {
            texts = Collections.singletonList(text);
        }
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position pos = getPosition();
        float height = fontRenderer.FONT_HEIGHT * scale * texts.size();
        for (int i = 0; i < texts.size(); i++) {
            String resultText = texts.get(i);
            float width = fontRenderer.getStringWidth(resultText) * scale;
            float x = pos.x - (isCentered ? width / 2f : 0);
            float y = pos.y - (isCentered ? height / 2f : 0) + i * fontRenderer.FONT_HEIGHT;
            drawText(resultText, x, y, scale, color, isShadow);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (!textSupplier.get().equals(lastText)) {
            this.lastText = textSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeString(lastText));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.lastText = buffer.readString(Short.MAX_VALUE);
            updateSize();
        }
    }
}
