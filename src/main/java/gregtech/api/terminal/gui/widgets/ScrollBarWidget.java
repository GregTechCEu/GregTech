package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class ScrollBarWidget extends Widget {

    protected final float min;
    protected final float max;
    protected final float dur;
    protected int xOffset;
    protected boolean draggedOnScrollBar;
    protected IGuiTexture background;
    protected IGuiTexture buttonTexture;
    protected int buttonWidth;
    protected int buttonHeight;
    protected Consumer<Float> onChanged;
    protected boolean isClient;

    public ScrollBarWidget(int x, int y, int width, int height, float min, float max, float dur) {
        super(new Position(x, y), new Size(width, height));
        this.max = max;
        this.min = min;
        this.dur = dur;
        this.xOffset = width / 2;
        this.buttonTexture = new ColorRectTexture(-1);
        this.buttonWidth = Math.max((int) (width / ((max - min) / dur)), 5);
        this.buttonHeight = height;
    }

    public ScrollBarWidget setOnChanged(Consumer<Float> onChanged, boolean isClient) {
        this.onChanged = onChanged;
        this.isClient = isClient;
        return this;
    }

    public ScrollBarWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public ScrollBarWidget setInitValue(float value) {
        if (value >= min && value <= max) {
            this.xOffset = (int) ((value - min) / (max - min) * (this.getSize().width - buttonWidth));
        }
        return this;
    }

    public ScrollBarWidget setButtonTexture(TextureArea buttonTexture, int buttonWidth, int buttonHeight) {
        this.buttonTexture = buttonTexture;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (this.background != null) {
            this.background.draw(x, y, width, height);
        } else {
            drawBorder(x - 1, y - 1, width + 2, height + 2, -1, 1);
        }
        if (this.buttonTexture != null) {
            this.buttonTexture.draw(x + xOffset, y + (height - buttonHeight) / 2f, buttonWidth, buttonHeight);
        }
    }

    private boolean isOnScrollPane(int mouseX, int mouseY) {
        Position position = this.getPosition();
        Size size = this.getSize();
        return isMouseOver(position.x, position.y, size.width, buttonHeight, mouseX, mouseY);
    }

    private float getValue() {
        return (float) (min +
                Math.floor((max - min) * xOffset * 1.0f / (this.getSize().width - buttonWidth) / dur) * dur);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isOnScrollPane(mouseX, mouseY)) {
            this.xOffset = MathHelper.clamp(mouseX - this.getPosition().x - buttonWidth / 2, 0,
                    this.getSize().width - buttonWidth);
            this.draggedOnScrollBar = true;
        }
        return this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (draggedOnScrollBar) {
            this.xOffset = MathHelper.clamp(mouseX - this.getPosition().x - buttonWidth / 2, 0,
                    this.getSize().width - buttonWidth);
            if (onChanged != null) {
                onChanged.accept(getValue());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (this.draggedOnScrollBar) {
            if (!isClient) {
                this.writeClientAction(2, packetBuffer -> packetBuffer.writeFloat(getValue()));
            }
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
