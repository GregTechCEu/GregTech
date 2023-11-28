package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;

import net.minecraft.util.math.MathHelper;

import mezz.jei.api.gui.IGhostIngredientHandler.Target;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ScrollableListWidget extends AbstractWidgetGroup {

    protected static final int scrollPaneWidth = 10;

    protected int totalListHeight;
    protected int slotHeight;
    protected int scrollOffset;
    protected int lastMouseX;
    protected int lastMouseY;
    protected boolean draggedOnScrollBar;

    public ScrollableListWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    @Override
    protected boolean recomputeSize() {
        updateElementPositions();
        return false;
    }

    private void addScrollOffset(int offset) {
        this.scrollOffset = MathHelper.clamp(scrollOffset + offset, 0, totalListHeight - getSize().height);
        updateElementPositions();
    }

    private boolean isOnScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - scrollPaneWidth, pos.y, scrollPaneWidth, size.height, mouseX, mouseY);
    }

    @Override
    protected void onPositionUpdate() {
        updateElementPositions();
    }

    private void updateElementPositions() {
        Position position = getPosition();
        int currentPosY = position.y - scrollOffset;
        int totalListHeight = 0;
        for (Widget widget : widgets) {
            Position childPosition = new Position(position.x, currentPosY);
            widget.setParentPosition(childPosition);
            currentPosY += widget.getSize().getHeight();
            totalListHeight += widget.getSize().getHeight();
        }
        this.totalListHeight = totalListHeight;
        this.slotHeight = widgets.isEmpty() ? 0 : totalListHeight / widgets.size();
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        // make sure mouse is not hovered on any element when outside of bounds,
        // since foreground rendering is not scissored,
        // because cut tooltips don't really look nice
        if (!isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        // make sure mouse is not hovered on any element when outside of bounds
        if (!isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }
        int finalMouseX = mouseX;
        int finalMouseY = mouseY;
        Position position = getPosition();
        Size size = getSize();
        int paneSize = scrollPaneWidth;
        int scrollX = position.x + size.width - paneSize;
        GuiTextures.SLIDER_BACKGROUND_VERTICAL.draw(scrollX + 1, position.y + 1, paneSize - 2, size.height - 2);

        int maxScrollOffset = totalListHeight - getSize().height + 2;
        float scrollPercent = maxScrollOffset == 0 ? 0 : scrollOffset / (maxScrollOffset * 1.0f);
        int scrollSliderHeight = 14;
        int scrollSliderY = Math.round(position.y + (size.height - scrollSliderHeight) * scrollPercent);
        GuiTextures.SLIDER_ICON.draw(scrollX + 1, scrollSliderY + 2, paneSize - 2, scrollSliderHeight);

        RenderUtil.useScissor(position.x, position.y, size.width - paneSize, size.height,
                () -> super.drawInBackground(finalMouseX, finalMouseY, partialTicks, context));
    }

    @Override
    public boolean isWidgetClickable(final Widget widget) {
        if (!super.isWidgetClickable(widget)) {
            return false;
        }
        return isWidgetOverlapsScissor(widget);
    }

    private boolean isPositionInsideScissor(int mouseX, int mouseY) {
        return isMouseOverElement(mouseX, mouseY) && !isOnScrollPane(mouseX, mouseY);
    }

    private boolean isWidgetOverlapsScissor(Widget widget) {
        final Position position = widget.getPosition();
        final Size size = widget.getSize();
        final int x0 = position.x;
        final int y0 = position.y;
        final int x1 = position.x + size.width - 1;
        final int y1 = position.y + size.height - 1;
        return isPositionInsideScissor(x0, y0) ||
                isPositionInsideScissor(x0, y1) ||
                isPositionInsideScissor(x1, y0) ||
                isPositionInsideScissor(x1, y1);
    }

    private boolean isBoxInsideScissor(Rectangle rectangle) {
        return isPositionInsideScissor(rectangle.x, rectangle.y) &&
                isPositionInsideScissor(rectangle.x + rectangle.width - 1, rectangle.y + rectangle.height - 1);
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            int direction = -MathHelper.clamp(wheelDelta, -1, 1);
            int moveDelta = direction * (slotHeight / 2);
            addScrollOffset(moveDelta);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (isOnScrollPane(mouseX, mouseY)) {
            this.draggedOnScrollBar = true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int mouseDelta = (mouseY - lastMouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (draggedOnScrollBar) {
            addScrollOffset(mouseDelta);
            return true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseDragged(mouseX, mouseY, button, timeDragged);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.draggedOnScrollBar = false;
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.getIngredientOverMouse(mouseX, mouseY);
        }
        return null;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        // for phantom targets, show only ones who are fully inside scissor box to avoid visual glitches
        return super.getPhantomTargets(ingredient).stream()
                .filter(it -> isBoxInsideScissor(it.getArea()))
                .collect(Collectors.toList());
    }
}
