package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorWidget extends WidgetGroup {
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int alpha = 255;
    private Consumer<Integer> onColorChanged;
    private final int barWidth;
    private final int barHeight;
    private final CircleButtonWidget redButton;
    private final CircleButtonWidget greenButton;
    private final CircleButtonWidget blueButton;
    private final CircleButtonWidget alphaButton;
    private int lastMouseX;
    private CircleButtonWidget dragged;
    private Supplier<Integer> colorSupplier;
    private boolean isClient;

    public ColorWidget(int x, int y, int barWidth, int barHeight) {
        super(new Position(x, y), new Size(barWidth + 35, 3 * (barHeight + 5) + 10));
        this.barWidth = barWidth;
        this.barHeight = barHeight;
        IGuiTexture textFieldBackground = new ColorRectTexture(0x9f000000);
        TextFieldWidget redField = new TextFieldWidget(barWidth + 5, 0, 30, barHeight, textFieldBackground, null, null)
                .setTextResponder((t) -> {
                    setRed(t.isEmpty() ? 0 : Integer.parseInt(t));
                    if (onColorChanged != null) {
                        onColorChanged.accept(getColor());
                    }
                    writeClientAction(2, buffer -> buffer.writeInt(getColor()));
                }, true)
                .setTextSupplier(() -> Integer.toString(red), true)
                .setValidator(this::checkValid);
        TextFieldWidget greenField = new TextFieldWidget(barWidth + 5, barHeight + 5, 30, barHeight, textFieldBackground, null, null)
                .setTextResponder((t) -> {
                    setGreen(t.isEmpty() ? 0 : Integer.parseInt(t));
                    if (onColorChanged != null) {
                        onColorChanged.accept(getColor());
                    }
                    writeClientAction(2, buffer -> buffer.writeInt(getColor()));
                }, true)
                .setTextSupplier(() -> Integer.toString(green), true)
                .setValidator(this::checkValid);
        TextFieldWidget blueField = new TextFieldWidget(barWidth + 5, (barHeight + 5) * 2, 30, barHeight, textFieldBackground, null, null)
                .setTextResponder((t) -> {
                    setBlue(t.isEmpty() ? 0 : Integer.parseInt(t));
                    if (onColorChanged != null) {
                        onColorChanged.accept(getColor());
                    }
                    writeClientAction(2, buffer -> buffer.writeInt(getColor()));
                }, true)
                .setTextSupplier(() -> Integer.toString(blue), true)
                .setValidator(this::checkValid);
        TextFieldWidget alphaField = new TextFieldWidget(barWidth + 5, (barHeight + 5) * 3, 30, barHeight, textFieldBackground, null, null)
                .setTextResponder((t) -> {
                    setAlpha(t.isEmpty() ? 0 : Integer.parseInt(t));
                    if (onColorChanged != null) {
                        onColorChanged.accept(getColor());
                    }
                    writeClientAction(2, buffer -> buffer.writeInt(getColor()));
                }, true)
                .setTextSupplier(() -> Integer.toString(alpha), true)
                .setValidator(this::checkValid);
        this.addWidget(redField);
        this.addWidget(greenField);
        this.addWidget(blueField);
        this.addWidget(alphaField);
        redButton = new CircleButtonWidget(barWidth, barHeight / 2, 4, 1, 0).setFill(0xffff0000).setStrokeAnima(-1);
        greenButton = new CircleButtonWidget(barWidth, barHeight / 2 + barHeight + 5, 4, 1, 0).setFill(0xff00ff00).setStrokeAnima(-1);
        blueButton = new CircleButtonWidget(barWidth, barHeight / 2 + 2 * (barHeight + 5), 4, 1, 0).setFill(0xff0000ff).setStrokeAnima(-1);
        alphaButton = new CircleButtonWidget(barWidth, barHeight / 2 + 3 * (barHeight + 5), 4, 1, 0).setFill(-1).setStrokeAnima(-1);
        this.addWidget(redButton);
        this.addWidget(greenButton);
        this.addWidget(blueButton);
        this.addWidget(alphaButton);
    }

    public ColorWidget setOnColorChanged(Consumer<Integer> onColorChanged) {
        this.onColorChanged = onColorChanged;
        return this;
    }

    public ColorWidget setColorSupplier(Supplier<Integer> colorSupplier, boolean isClient) {
        this.colorSupplier = colorSupplier;
        this.isClient = isClient;
        return this;
    }

    public ColorWidget setStartColor(int color) {
        setRed((color >> 16) & 0xFF);
        setGreen((color >> 8) & 0xFF);
        setBlue(color & 0xFF);
        setAlpha((color >> 24) & 0xFF);
        return this;
    }

    public int getColor() {
        return (alpha << 24) | (red << 16) | (green << 8) | (blue);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!isClient && colorSupplier != null) {
            int c = colorSupplier.get();
            int r = (c & 0x00ff0000) >>> 16;
            int g = (c & 0x0000ff00) >>> 8;
            int b = (c & 0x000000ff);
            int a = (c & 0xff000000) >>> 24;
            if (r != red || g != green || b != blue || a != alpha) {
                setRed(r);
                setGreen(g);
                setBlue(b);
                setAlpha(a);
                writeUpdateInfo(2, buffer -> buffer.writeInt(c));
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClient && colorSupplier != null) {
            int c = colorSupplier.get();
            int r = (c & 0x00ff0000) >>> 16;
            int g = (c & 0x0000ff00) >>> 8;
            int b = (c & 0x000000ff);
            int a = (c & 0xff000000) >>> 24;
            if (r != red || g != green || b != blue || a != alpha) {
                setRed(r);
                setGreen(g);
                setBlue(b);
                setAlpha(a);
                writeClientAction(2, buffer -> buffer.writeInt(c));
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        handleColor(id, buffer);
    }

    private void handleColor(int id, PacketBuffer buffer) {
        if (id == 2) {
            int c = buffer.readInt();
            int r = (c & 0x00ff0000) >>> 16;
            int g = (c & 0x0000ff00) >>> 8;
            int b = (c & 0x000000ff);
            int a = (c & 0xff000000) >>> 24;
            if (r != red || g != green || b != blue || a != alpha) {
                setRed(r);
                setGreen(g);
                setBlue(b);
                setAlpha(a);
                if (onColorChanged != null) {
                    onColorChanged.accept(getColor());
                }
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        handleColor(id, buffer);
    }

    private void setRed(int red) {
        if (this.red != red) {
            this.red = red;
            redButton.setSelfPosition(new Position(red * barWidth / 255 - 4, redButton.getSelfPosition().y));
        }
    }

    private void setGreen(int green) {
        if (this.green != green) {
            this.green = green;
            greenButton.setSelfPosition(new Position(green * barWidth / 255 - 4, greenButton.getSelfPosition().y));
        }
    }

    private void setBlue(int blue) {
        if (this.blue != blue) {
            this.blue = blue;
            blueButton.setSelfPosition(new Position(blue * barWidth / 255 - 4, blueButton.getSelfPosition().y));
        }
    }

    private void setAlpha(int alpha) {
        if (this.alpha != alpha) {
            this.alpha = alpha;
            alphaButton.setSelfPosition(new Position(alpha * barWidth / 255 - 4, alphaButton.getSelfPosition().y));
        }
    }

    private boolean checkValid(String input) {
        if (input.length() > 3) return false;
        if (input.isEmpty()) return true;
        try {
            int value = Integer.parseInt(input);
            if (value >= 0 && value <= 255) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        drawGradientRect(x, y + 2, barWidth, 5, (255 << 24) | (0) | (green << 8) | (blue), (255 << 24) | (255 << 16) | (green << 8) | (blue), true);
        drawGradientRect(x, y + barHeight + 5 + 2, barWidth, 5, (255 << 24) | (red << 16) | (0) | (blue), (255 << 24) | (red << 16) | (255 << 8) | (blue), true);
        drawGradientRect(x, y + 2 * (barHeight + 5) + 2, barWidth, 5, (255 << 24) | (red << 16) | (green << 8) | (0), (255 << 24) | (red << 16) | (green << 8) | (255), true);
        drawGradientRect(x, y + 3 * (barHeight + 5) + 2, barWidth, 5, (0) | (red << 16) | (green << 8) | (blue), (255 << 24) | (red << 16) | (green << 8) | (blue), true);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        lastMouseX = mouseX;
        dragged = null;
        if (redButton.isMouseOverElement(mouseX, mouseY)) {
            dragged = redButton;
            return true;
        } else if (greenButton.isMouseOverElement(mouseX, mouseY)) {
            dragged = greenButton;
            return true;
        } else if (blueButton.isMouseOverElement(mouseX, mouseY)) {
            dragged = blueButton;
            return true;
        } else if (alphaButton.isMouseOverElement(mouseX, mouseY)) {
            dragged = alphaButton;
            return true;
        }
        boolean flag = false;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible() && widget.mouseClicked(mouseX, mouseY, button)) {
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int xDelta = mouseX - lastMouseX;
        lastMouseX = mouseX;
        if (dragged != null) {
            int newX = MathHelper.clamp(dragged.getSelfPosition().x + 4 + xDelta, 0, barWidth);
            if (dragged == redButton) {
                setRed(newX * 255 / barWidth);
            } else if (dragged == greenButton) {
                setGreen(newX * 255 / barWidth);
            } else if (dragged == blueButton) {
                setBlue(newX * 255 / barWidth);
            } else if (dragged == alphaButton) {
                setAlpha(newX * 255 / barWidth);
            }
            if (onColorChanged != null) {
                onColorChanged.accept(getColor());
            }
            writeClientAction(2, buffer -> buffer.writeInt(getColor()));
            dragged.setSelfPosition(new Position(newX - 4, dragged.getSelfPosition().y));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        dragged = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
