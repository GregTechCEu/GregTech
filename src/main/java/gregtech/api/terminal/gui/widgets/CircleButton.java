package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Collections;
import java.util.function.Consumer;

public class CircleButton extends Widget {
    private int hoverTick;
    private boolean isHover;
    private String hoverText;
    private IGuiTexture icon;
    private Consumer<ClickData> onPressCallback;
    private final int[] colors = {
            new Color(146, 146, 146).getRGB(),
            new Color(39, 232, 141).getRGB(),
            new Color(255, 255, 255).getRGB(),
    };

    public CircleButton(int x, int y, int r) {
        super(new Position(x - r, y - r), new Size(2 * r, 2 * r));
    }

    public CircleButton setIcon(IGuiTexture icon) {
        this.icon = icon;
        return this;
    }

    public CircleButton setHoverText(String hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public CircleButton setColors(int stroke, int strokeAnima, int fill) {
        colors[0] = stroke;
        colors[1] = strokeAnima;
        colors[2] = fill;
        return this;
    }

    public CircleButton setFillColors(int fill) {
        colors[2] = fill;
        return this;
    }

    public CircleButton setClickListener(Consumer<ClickData> onPressed) {
        this.onPressCallback = onPressed;
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

        RenderUtil.renderCircle(x, y, r, colors[0], segments);
        isHover = this.isMouseOverElement(mouseX, mouseY);
        if (isHover || hoverTick != 0) {
            RenderUtil.renderSector(x, y, r, colors[1], segments, 0, (int) (segments * ((hoverTick + partialTicks) / 8)));
        }
        RenderUtil.renderCircle(x, y, r - 2, colors[2], segments);
        if (icon != null) {
            icon.draw(x - 8, y - 8, 16, 16);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
           this.drawHoveringText(ItemStack.EMPTY, Collections.singletonList(I18n.format(hoverText)), 300, mouseX, mouseY);
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
