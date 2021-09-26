package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CircleButtonWidget extends Widget {
    protected int border;
    protected int hoverTick;
    protected boolean isHover;
    protected String[] hoverText;
    protected IGuiTexture icon;
    protected IGuiTexture hover;
    protected final int iconSize;
    protected Consumer<ClickData> onPressCallback;
    protected final int[] colors = {
            new Color(146, 146, 146).getRGB(),
            new Color(39, 232, 141).getRGB(),
            new Color(255, 255, 255).getRGB(),
    };

    public CircleButtonWidget(int x, int y, int r, int border, int iconSize) {
        super(new Position(x - r, y - r), new Size(2 * r, 2 * r));
        this.border = border;
        this.iconSize = iconSize;
    }

    public CircleButtonWidget(int x, int y) {
        this(x, y, 12, 2, 16);
    }

    public CircleButtonWidget setIcon(IGuiTexture icon) {
        this.icon = icon;
        return this;
    }

    public CircleButtonWidget setHoverText(String... hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public CircleButtonWidget setColors(int stroke, int strokeAnima, int fill) {
        colors[0] = stroke;
        colors[1] = strokeAnima;
        colors[2] = fill;
        return this;
    }

    public CircleButtonWidget setStroke(int stroke) {
        colors[0] = stroke;
        return this;
    }

    public CircleButtonWidget setStrokeAnima(int strokeAnima) {
        colors[1] = strokeAnima;
        return this;
    }

    public CircleButtonWidget setFill(int fill) {
        colors[2] = fill;
        return this;
    }

    public CircleButtonWidget setClickListener(Consumer<ClickData> onPressed) {
        this.onPressCallback = onPressed;
        return this;
    }

    public CircleButtonWidget setHoverIcon(IGuiTexture hover) {
        this.hover = hover;
        return this;
    }

    @Override
    public void updateScreen() {
        if (isHover) {
            if (hoverTick < 8) {
                hoverTick += 1;
            }
        } else {
            if (hoverTick > 0) {
                hoverTick -= 1;
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int r = this.getSize().getHeight() / 2;
        int x = this.getPosition().x + r;
        int y = this.getPosition().y + r;
        int segments = 24;

        drawTorus(x, y, r, r - border, colors[0], segments, 0, segments);
        isHover = this.isMouseOverElement(mouseX, mouseY);
        if (isHover || hoverTick != 0) {
            drawTorus(x, y, r, r - border, colors[1], segments, 0, (int) (segments * ((hoverTick + partialTicks) / 8)));
        }
        drawCircle(x, y, r - border, colors[2], segments);
        if (isHover && hover != null) {
            hover.draw(x - iconSize / 2f, y - iconSize / 2f, iconSize, iconSize);
        }
        if (icon != null) {
            icon.draw(x - iconSize / 2f, y - iconSize / 2f, iconSize, iconSize);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (hoverText != null && hoverText.length > 0 && this.isMouseOverElement(mouseX, mouseY)) {
           this.drawHoveringText(ItemStack.EMPTY, Arrays.stream(hoverText).map(I18n::format).collect(Collectors.toList()), 300, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), false);
            writeClientAction(1, clickData::writeToBuf);
            playButtonClickSound();
            if (onPressCallback != null) {
                onPressCallback.accept(new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), true));
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            if (onPressCallback != null) {
                onPressCallback.accept(clickData);
            }
        }
    }

}
