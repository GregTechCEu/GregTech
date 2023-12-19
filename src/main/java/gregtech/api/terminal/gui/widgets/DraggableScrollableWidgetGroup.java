package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.IDraggable;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.util.math.MathHelper;

public class DraggableScrollableWidgetGroup extends WidgetGroup {

    protected int scrollXOffset;
    protected int scrollYOffset;
    protected int xBarHeight;
    protected int yBarWidth;
    protected boolean draggable;
    protected IGuiTexture background;
    protected int maxHeight;
    protected int maxWidth;
    protected IGuiTexture xBarB;
    protected IGuiTexture xBarF;
    protected IGuiTexture yBarB;
    protected IGuiTexture yBarF;
    protected boolean focus;
    protected Widget draggedWidget;
    protected boolean useScissor;

    private int lastMouseX;
    private int lastMouseY;
    private boolean draggedPanel;
    private boolean draggedOnXScrollBar;
    private boolean draggedOnYScrollBar;

    public DraggableScrollableWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        maxHeight = height;
        maxWidth = width;
        useScissor = true;
    }

    public DraggableScrollableWidgetGroup setXScrollBarHeight(int xBar) {
        this.xBarHeight = xBar;
        return this;
    }

    public DraggableScrollableWidgetGroup setYScrollBarWidth(int yBar) {
        this.yBarWidth = yBar;
        return this;
    }

    public DraggableScrollableWidgetGroup setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public DraggableScrollableWidgetGroup setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public DraggableScrollableWidgetGroup setXBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.xBarB = background;
        this.xBarF = bar;
        return this;
    }

    public DraggableScrollableWidgetGroup setYBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.yBarB = background;
        this.yBarF = bar;
        return this;
    }

    public void setUseScissor(boolean useScissor) {
        this.useScissor = useScissor;
    }

    public int getScrollYOffset() {
        return scrollYOffset;
    }

    public int getScrollXOffset() {
        return scrollXOffset;
    }

    @Override
    public void addWidget(Widget widget) {
        maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getSelfPosition().y);
        maxWidth = Math.max(maxWidth, widget.getSize().width + widget.getSelfPosition().x);
        Position newPos = widget.addSelfPosition(-scrollXOffset, -scrollYOffset);
        widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
        widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        super.addWidget(widget);
    }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
        computeMax();
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
        maxHeight = getSize().height;
        maxWidth = getSize().width;
        scrollXOffset = 0;
        scrollYOffset = 0;
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        maxHeight = Math.max(size.height, maxHeight);
        maxWidth = Math.max(size.width, maxWidth);
        // computeMax();
        for (Widget widget : widgets) {
            Position newPos = widget.getSelfPosition();
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
    }

    public void computeMax() {
        int mh = 0;
        int mw = 0;
        for (Widget widget : widgets) {
            mh = Math.max(mh, widget.getSize().height + widget.getSelfPosition().y + scrollYOffset);
            mw = Math.max(mw, widget.getSize().width + widget.getSelfPosition().x + scrollXOffset);
        }
        int offsetY = 0;
        int offsetX = 0;
        if (mh > getSize().height) {
            offsetY = maxHeight - mh;
            maxHeight = mh;
            if (scrollYOffset - offsetY < 0) {
                offsetY = scrollYOffset;
            }
            scrollYOffset -= offsetY;
        } else if (mh < getSize().height) {
            offsetY = maxHeight - getSize().height;
            maxHeight = getSize().height;
            if (scrollYOffset - offsetY < 0) {
                offsetY = scrollYOffset;
            }
            scrollYOffset -= offsetY;
        }
        if (mw > getSize().width) {
            offsetX = maxWidth - mw;
            maxWidth = mw;
            if (scrollXOffset - offsetX < 0) {
                offsetX = scrollXOffset;
            }
            scrollXOffset -= offsetX;
        } else if (mw < getSize().width) {
            offsetX = maxWidth - getSize().width;
            maxWidth = getSize().width;
            if (scrollXOffset - offsetX < 0) {
                offsetX = scrollXOffset;
            }
            scrollXOffset -= offsetX;
        }
        if (offsetX != 0 || offsetY != 0) {
            for (Widget widget : widgets) {
                Position newPos = widget.addSelfPosition(offsetX, offsetY);
                widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
                widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
            }
        }
    }

    protected int getMaxHeight() {
        return maxHeight + xBarHeight;
    }

    protected int getMaxWidth() {
        return maxWidth + yBarWidth;
    }

    public int getWidgetBottomHeight() {
        int y = 0;
        for (Widget widget : widgets) {
            y = Math.max(y, widget.getSize().height + widget.getSelfPosition().y);
        }
        return y;
    }

    protected void setScrollXOffset(int scrollXOffset) {
        if (scrollXOffset == this.scrollXOffset) return;
        int offset = scrollXOffset - this.scrollXOffset;
        this.scrollXOffset = scrollXOffset;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition(-offset, 0);
            widget.setVisible(newPos.x < getSize().width - yBarWidth && newPos.x + widget.getSize().width > 0);
        }
    }

    protected void setScrollYOffset(int scrollYOffset) {
        if (scrollYOffset == this.scrollYOffset) return;
        if (scrollYOffset < 0) scrollYOffset = 0;
        int offset = scrollYOffset - this.scrollYOffset;
        this.scrollYOffset = scrollYOffset;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition(0, -offset);
            widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
        }
    }

    private boolean isOnXScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x, pos.y + size.height - xBarHeight, size.width, xBarHeight, mouseX, mouseY);
    }

    private boolean isOnYScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - yBarWidth, pos.y, yBarWidth, size.height, mouseX, mouseY);
    }

    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (background != null) {
            background.draw(x, y, width, height);
        }
        if (useScissor) {
            RenderUtil.useScissor(x, y, width - yBarWidth, height - xBarHeight, () -> {
                if (!hookDrawInBackground(mouseX, mouseY, partialTicks, context)) {
                    super.drawInBackground(mouseX, mouseY, partialTicks, context);
                }
            });
        } else {
            if (!hookDrawInBackground(mouseX, mouseY, partialTicks, context)) {
                super.drawInBackground(mouseX, mouseY, partialTicks, context);
            }
        }

        if (xBarHeight > 0) {
            if (xBarB != null) {
                xBarB.draw(x, y - xBarHeight, width, xBarHeight);
            }
            if (xBarF != null) {
                int barWidth = (int) (width * 1.0f / getMaxWidth() * width);
                xBarF.draw(x + scrollXOffset * width * 1.0f / getMaxWidth(), y + height - xBarHeight, barWidth,
                        xBarHeight);
            }
        }
        if (yBarWidth > 0) {
            if (yBarB != null) {
                yBarB.draw(x + width - yBarWidth, y, yBarWidth, height);
            }
            if (yBarF != null) {
                int barHeight = (int) (height * 1.0f / getMaxHeight() * height);
                yBarF.draw(x + width - yBarWidth, y + scrollYOffset * height * 1.0f / getMaxHeight(), yBarWidth,
                        barHeight);
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (xBarHeight > 0 && isOnXScrollPane(mouseX, mouseY)) {
            this.draggedOnXScrollBar = true;
            focus = true;
            return true;
        } else if (yBarWidth > 0 && isOnYScrollPane(mouseX, mouseY)) {
            this.draggedOnYScrollBar = true;
            focus = true;
            return true;
        } else if (isMouseOverElement(mouseX, mouseY)) {
            focus = true;
            if (checkClickedDragged(mouseX, mouseY, button)) {
                return true;
            }
            if (draggable) {
                this.draggedPanel = true;
                return true;
            }
            return false;
        } else if (checkClickedDragged(mouseX, mouseY, button)) {
            return true;
        }
        focus = false;
        return false;
    }

    protected boolean checkClickedDragged(int mouseX, int mouseY, int button) {
        draggedWidget = null;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible()) {
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                } else if (widget instanceof IDraggable && ((IDraggable) widget).allowDrag(mouseX, mouseY, button)) {
                    draggedWidget = widget;
                    ((IDraggable) widget).startDrag(mouseX, mouseY);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (super.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
            int moveDelta = -MathHelper.clamp(wheelDelta, -1, 1) * ConfigHolder.client.guiConfig.scrollSpeed;
            if (getMaxHeight() - getSize().height > 0 || scrollYOffset > getMaxHeight() - getSize().height) {
                setScrollYOffset(MathHelper.clamp(scrollYOffset + moveDelta, 0, getMaxHeight() - getSize().height));
            }
            return true;
        }
        focus = false;
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int deltaX = mouseX - lastMouseX;
        int deltaY = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (draggedOnXScrollBar &&
                (getMaxWidth() - getSize().width > 0 || scrollYOffset > getMaxWidth() - getSize().width)) {
            setScrollXOffset(MathHelper.clamp(scrollXOffset + deltaX * getMaxWidth() / getSize().width, 0,
                    getMaxWidth() - getSize().width));
            return true;
        } else if (draggedOnYScrollBar &&
                (getMaxHeight() - getSize().height > 0 || scrollYOffset > getMaxHeight() - getSize().height)) {
                    setScrollYOffset(MathHelper.clamp(scrollYOffset + deltaY * getMaxHeight() / getSize().height, 0,
                            getMaxHeight() - getSize().height));
                    return true;
                } else
            if (draggedWidget != null) {
                if (((IDraggable) draggedWidget).dragging(mouseX, mouseY, deltaX, deltaY)) {
                    draggedWidget.addSelfPosition(deltaX, deltaY);
                }
                computeMax();
                return true;
            } else if (draggedPanel) {
                setScrollXOffset(MathHelper.clamp(scrollXOffset - deltaX, 0,
                        Math.max(getMaxWidth() - yBarWidth - getSize().width, 0)));
                setScrollYOffset(MathHelper.clamp(scrollYOffset - deltaY, 0,
                        Math.max(getMaxHeight() - xBarHeight - getSize().height, 0)));
                return true;
            }
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (draggedOnXScrollBar) {
            draggedOnXScrollBar = false;
        } else if (draggedOnYScrollBar) {
            draggedOnYScrollBar = false;
        } else if (draggedWidget != null) {
            ((IDraggable) draggedWidget).endDrag(mouseX, mouseY);
            draggedWidget = null;
        } else if (draggedPanel) {
            draggedPanel = false;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return true;
    }
}
