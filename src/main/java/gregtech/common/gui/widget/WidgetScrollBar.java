package gregtech.common.gui.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class WidgetScrollBar extends Widget {

    protected final float min;
    protected final float max;
    protected final float dur;
    protected int xOffset;
    protected boolean draggedOnScrollBar;
    protected TextureArea buttonTexture;
    protected int buttonWidth;
    protected int buttonHeight;
    protected int lineColor;
    protected String title;
    protected int titleColor;
    protected Consumer<Float> onChanged;

    public WidgetScrollBar(int x, int y, int width, float min, float max, float dur, Consumer<Float> onChanged) {
        super(new Position(x, y), new Size(width, 20));
        this.max = max;
        this.min = min;
        this.dur = dur;
        this.xOffset = width / 2;
        this.buttonTexture = GuiTextures.VANILLA_BUTTON.getSubArea(0.0D, 0.0D, 1.0D, 0.5D);
        this.buttonWidth = 6;
        this.buttonHeight = 8;
        this.lineColor = 0XFF000000;
        this.title = "";
        this.titleColor = 0XFFFFFFFF;
        this.onChanged = onChanged;
    }

    public WidgetScrollBar setInitValue(float value) {
        if (value >= min && value <= max) {
            this.xOffset = (int) ((value - min) / (max - min) * this.getSize().width);
        }
        return this;
    }

    public WidgetScrollBar setButtonTexture(TextureArea buttonTexture, int buttonWidth, int buttonHeight) {
        this.buttonTexture = buttonTexture;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        return this;
    }

    public WidgetScrollBar setLineColor(int lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public WidgetScrollBar setTitle(String title, int titleColor) {
        this.title = title;
        this.titleColor = titleColor;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = this.getPosition();
        Size size = this.getSize();
        RenderUtil.renderRect(position.x, position.y + 15 - 0.5f, size.width, 1, 0, lineColor);
        if (this.buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) this.buttonTexture).drawHorizontalCutSubArea(position.x + xOffset - buttonWidth / 2,
                    position.y + 15 - buttonHeight / 2, buttonWidth, buttonHeight, 0.0D, 1.0D);
        } else {
            this.buttonTexture.drawSubArea(position.x + xOffset - buttonWidth * 0.5f,
                    position.y + 15 - buttonHeight * 0.5f, buttonWidth, buttonHeight, 0.0D, 0.0D, 1.0D, 1.0D);
        }

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String text = I18n.format(this.title);
        text += ": " + new DecimalFormat("#.00").format(getValue());
        fontRenderer.drawString(text, position.x + size.width / 2 - fontRenderer.getStringWidth(text) / 2,
                position.y - 3 + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, this.titleColor);
    }

    private boolean isOnScrollPane(int mouseX, int mouseY) {
        Position position = this.getPosition();
        Size size = this.getSize();
        return isMouseOver(position.x - buttonWidth / 2, position.y + 15 - buttonHeight / 2,
                size.width + buttonWidth / 2, buttonHeight, mouseX, mouseY);
    }

    private float getValue() {
        return (float) (min + Math.floor((max - min) * xOffset * 1.0f / this.getSize().width / dur) * dur);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isOnScrollPane(mouseX, mouseY)) {
            this.xOffset = mouseX - this.getPosition().x;
            this.draggedOnScrollBar = true;
        }
        return this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (draggedOnScrollBar) {
            Position position = this.getPosition();
            Size size = this.getSize();
            if (mouseX > position.x + size.width) {
                this.xOffset = size.width;
            } else if (mouseX < position.x) {
                this.xOffset = 0;
            } else {
                this.xOffset = mouseX - this.getPosition().x;
            }
            if (this.onChanged != null) {
                onChanged.accept(getValue());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (this.draggedOnScrollBar) {
            this.writeClientAction(2, packetBuffer -> packetBuffer.writeFloat(getValue()));
        }
        this.draggedOnScrollBar = false;
        return this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            float value = buffer.readFloat();
            if (this.onChanged != null) {
                onChanged.accept(value);
            }
        }
    }
}
