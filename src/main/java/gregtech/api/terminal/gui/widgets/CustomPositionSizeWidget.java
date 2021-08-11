package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.IDraggable;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import java.util.function.BiConsumer;

public class CustomPositionSizeWidget extends Widget implements IDraggable {
    private Widget controlled;
    private final int borderColor;
    private final int hoverColor;
    private final int border;
    private boolean dragUp;
    private boolean dragDown;
    private boolean dragLeft;
    private boolean dragRight;
    private boolean dragPos;

    private BiConsumer<Position, Size> onUpdated;


    public CustomPositionSizeWidget(Widget controlled, int borderColor, int hoverColor, int border) {
        super(controlled.getSelfPosition(), controlled.getSize());
        this.controlled = controlled;
        this.borderColor = borderColor;
        this.hoverColor = hoverColor;
        this.border = border;
    }

    public CustomPositionSizeWidget(int borderColor, int hoverColor, int border) {
        super(Position.ORIGIN, Size.ZERO);
        this.borderColor = borderColor;
        this.hoverColor = hoverColor;
        this.border = border;
    }

    public CustomPositionSizeWidget setControlled(Widget controlled) {
        this.controlled = controlled;
        if (controlled != null) {
            this.setSelfPosition(controlled.getSelfPosition());
            this.setSize(controlled.getSize());
        }
        return this;
    }

    public Widget getControlled() {
        return controlled;
    }

    public CustomPositionSizeWidget setOnUpdated(BiConsumer<Position, Size> onUpdated) {
        this.onUpdated = onUpdated;
        return this;
    }

    @Override
    public void updateScreen() {
        if (controlled != null) {
            Position pos = controlled.getSelfPosition();
            Size size = controlled.getSize();
            if (!this.getSelfPosition().equals(pos)) {
                this.setSelfPosition(pos);
            }
            if (this.getSize().equals(size)) {
                this.setSize(size);
            }
        }
    }

    private boolean hoverUp(int x, int y, int width, int height, int mouseX, int mouseY) {
        return isMouseOver(x, y, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x + width * 2 / 5, y, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x + width * 4 / 5, y, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x, y, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x + width - border, y, border, height / 5, mouseX, mouseY);
    }

    private boolean hoverDown(int x, int y, int width, int height, int mouseX, int mouseY) {
        return isMouseOver(x, y + height - border, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x + width * 2 / 5, y + height - border, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x + width * 4 / 5, y + height - border, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x, y + height * 4 / 5, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x + width - border, y + height * 4 / 5, border, height / 5, mouseX, mouseY);
    }

    private boolean hoverLeft(int x, int y, int width, int height, int mouseX, int mouseY) {
        return isMouseOver(x, y, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x, y + height * 2 / 5, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x, y + height * 4 / 5, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x, y, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x, y + height - border, width / 5, border, mouseX, mouseY);
    }

    private boolean hoverRight(int x, int y, int width, int height, int mouseX, int mouseY) {
        return isMouseOver(x + width - border, y, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x + width - border, y + height * 2 / 5, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x + width - border, y + height * 4 / 5, border, height / 5, mouseX, mouseY) ||
                isMouseOver(x + width * 4 / 5, y, width / 5, border, mouseX, mouseY) ||
                isMouseOver(x + width * 4 / 5, y + height - border, width / 5, border, mouseX, mouseY);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (controlled == null) return;
        int x = controlled.getPosition().x;
        int y = controlled.getPosition().y;
        int width = controlled.getSize().width;
        int height = controlled.getSize().height;

        boolean hoverUp = false;
        boolean hoverDown = false;
        boolean hoverLeft = false;
        boolean hoverRight = false;
        // UP
        if (dragUp || hoverUp(x, y, width, height, mouseX, mouseY)) {
            hoverUp = true;
        }
        if (dragDown || hoverDown(x, y, width, height, mouseX, mouseY)) {
            hoverDown = true;
        }
        if (dragLeft || hoverLeft(x, y, width, height, mouseX, mouseY)) {
            hoverLeft = true;
        }
        if (dragRight || hoverRight(x, y, width, height, mouseX, mouseY)) {
            hoverRight = true;
        }
        // UP
        drawSolidRect(x, y, width / 5, border, hoverUp && !hoverRight ? hoverColor : borderColor);
        drawSolidRect(x + width * 2 / 5, y, width / 5, border, hoverUp && !hoverLeft && !hoverRight ? hoverColor : borderColor);
        drawSolidRect(x + width * 4 / 5, y, width / 5, border, hoverUp && !hoverLeft ? hoverColor : borderColor);
        // DOWN
        drawSolidRect(x, y + height - border, width / 5, border, hoverDown && !hoverRight ? hoverColor : borderColor);
        drawSolidRect(x + width * 2 / 5, y + height - border, width / 5, border, hoverDown && !hoverLeft && !hoverRight ? hoverColor : borderColor);
        drawSolidRect(x + width * 4 / 5, y + height - border, width / 5, border, hoverDown && !hoverLeft ? hoverColor : borderColor);
        // LEFT
        drawSolidRect(x, y, border, height / 5, hoverLeft && !hoverDown ? hoverColor : borderColor);
        drawSolidRect(x, y + height * 2 / 5, border, height / 5, hoverLeft && !hoverDown && !hoverUp ? hoverColor : borderColor);
        drawSolidRect(x, y + height * 4 / 5, border, height / 5, hoverLeft && !hoverUp ? hoverColor : borderColor);
        // RIGHT
        drawSolidRect(x + width - border, y, border, height / 5, hoverRight && !hoverDown ? hoverColor : borderColor);
        drawSolidRect(x + width - border, y + height * 2 / 5, border, height / 5, hoverRight && !hoverDown && !hoverUp ? hoverColor : borderColor);
        drawSolidRect(x + width - border, y + height * 4 / 5, border, height / 5, hoverRight && !hoverUp ? hoverColor : borderColor);
    }

    @Override
    public boolean allowDrag(int mouseX, int mouseY, int button) {
        if (controlled == null || !isActive()) return false;
        int x = controlled.getPosition().x;
        int y = controlled.getPosition().y;
        int width = controlled.getSize().width;
        int height = controlled.getSize().height;
        if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
            // UP
            dragUp = hoverUp(x, y, width, height, mouseX, mouseY);
            // DOWN
            dragDown = hoverDown(x, y, width, height, mouseX, mouseY);
            // LEFT
            dragLeft = hoverLeft(x, y, width, height, mouseX, mouseY);
            // RIGHT
            dragRight = hoverRight(x, y, width, height, mouseX, mouseY);
            dragPos = !dragUp && !dragDown && !dragLeft && !dragRight;
            return true;
        }
        return false;
    }

    @Override
    public boolean dragging(int mouseX, int mouseY, int deltaX, int deltaY) {
        if (controlled == null || !isActive()) return false;
        int width = controlled.getSize().width;
        int height = controlled.getSize().height;
        int addX = 0, addY = 0;
        if (!dragPos) {
            if (dragUp) {
                addY = deltaY;
                height = Math.max(1, height - deltaY);
            }
            if (dragDown) {
                height = Math.max(1, height + deltaY);
            }
            if (dragLeft) {
                addX = deltaX;
                width = Math.max(1, width - deltaX);
            }
            if (dragRight) {
                width = Math.max(1, width + deltaX);
            }
            controlled.addSelfPosition(addX, addY);
            controlled.setSize(new Size(width, height));
        } else {
            controlled.addSelfPosition(deltaX, deltaY);
        }
        if (onUpdated != null) {
            onUpdated.accept(controlled.getSelfPosition(), controlled.getSize());
        }
        this.setSelfPosition(controlled.getSelfPosition());
        this.setSize(controlled.getSize());
        return false;
    }

    @Override
    public void endDrag(int mouseX, int mouseY) {
        dragDown = false;
        dragUp = false;
        dragLeft = false;
        dragRight = false;
        dragPos = false;
    }
}
