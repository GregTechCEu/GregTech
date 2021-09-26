package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Mouse;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RectButtonWidget extends CircleButtonWidget{
    private IGuiTexture pressedIcon;
    private BiConsumer<ClickData, Boolean> onPressed;
    private boolean isPressed;
    private Supplier<Boolean> supplier;
    private boolean isClient;


    public RectButtonWidget(int x, int y, int width, int height) {
        this(x, y, width, height,2);
    }

    public RectButtonWidget(int x, int y, int width, int height, int border) {
        super(x, y);
        setSelfPosition(new Position(x, y));
        setSize(new Size(width, height));
        this.border = border;
    }

    public RectButtonWidget setToggleButton(IGuiTexture pressedIcon, BiConsumer<ClickData, Boolean> onPressed) {
        this.pressedIcon = pressedIcon;
        this.onPressed = onPressed;
        return this;
    }

    public RectButtonWidget setToggleButton(IGuiTexture pressedIcon, Consumer<Boolean> onPressed) {
        this.pressedIcon = pressedIcon;
        this.onPressed = onPressed != null ? (c, p)-> onPressed.accept(p) : null;
        return this;
    }

    public RectButtonWidget setInitValue(boolean isPressed) {
        this.isPressed = isPressed;
        return this;
    }

    public RectButtonWidget setValueSupplier(boolean isClient, Supplier<Boolean> supplier) {
        this.isClient = isClient;
        this.supplier = supplier;
        return this;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClient && supplier != null) {
            isPressed = supplier.get();
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (!isClient && supplier != null) {
            if(supplier.get() != isPressed) {
                isPressed = !isPressed;
                writeUpdateInfo(1, buffer -> buffer.writeBoolean(isPressed));
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            isPressed = buffer.readBoolean();
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (onPressed == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else {
            if (isMouseOverElement(mouseX, mouseY)) {
                isPressed = !isPressed;
                if (!isClient) {
                    ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), false);
                    writeClientAction(1, buffer -> {
                        clickData.writeToBuf(buffer);
                        buffer.writeBoolean(isPressed);
                    });
                }
                playButtonClickSound();
                onPressed.accept(new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), true), isPressed);
                return true;
            }
            return false;
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (onPressed == null) {
            super.handleClientAction(id, buffer);
        } else {
            if (id == 1) {
                ClickData clickData = ClickData.readFromBuf(buffer);
                isPressed = buffer.readBoolean();
                onPressed.accept(clickData, isPressed);
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = this.getPosition().x;
        int y = this.getPosition().y;
        int width = this.getSize().width;
        int height = this.getSize().height;

        drawBorder(x + border, y + border, width - 2 * border, height - 2 * border, colors[0], border);
        isHover = this.isMouseOverElement(mouseX, mouseY);
        if (isHover || hoverTick != 0) {
            float per = Math. min ((hoverTick + partialTicks) / 8, 1);
            drawSolidRect(x, y, (int) (width * per), border, colors[1]);
            drawSolidRect(x + width - border, y, border, (int) (height * per), colors[1]);
            drawSolidRect((int) ((1 - per) * width) + x, y + height - border, (int) (width * per), border, colors[1]);
            drawSolidRect(x, (int) ((1 - per) * height) + y, border, (int) (height * per), colors[1]);
        }
        drawSolidRect(x + border, y + border, width - 2 * border, height - 2 * border, colors[2]);
        if (isHover && hover != null) {
            hover.draw(x + border, y + border, width - 2 * border, height - 2 * border);
        }
        if (isPressed) {
            if (pressedIcon != null) {
                pressedIcon.draw(x + border, y + border, width - 2 * border, height - 2 * border);
            }
        } else if (icon != null) {
            icon.draw(x + border, y + border, width - 2 * border, height - 2 * border);
        }
    }
}
