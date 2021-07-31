package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class ScrollablePanelWidgetGroup extends WidgetGroup {
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

    private int lastMouseX;
    private int lastMouseY;
    private boolean draggedPanel;
    private boolean draggedOnXScrollBar;
    private boolean draggedOnYScrollBar;


    public ScrollablePanelWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        maxHeight = height;
        maxWidth = width;
    }

    public ScrollablePanelWidgetGroup availableXScrollBarHeight(int xBar) {
        this.xBarHeight = xBar;
        return this;
    }

    public ScrollablePanelWidgetGroup availableYScrollBarWidth(int yBar) {
        this.yBarWidth = yBar;
        return this;
    }

    public ScrollablePanelWidgetGroup setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public ScrollablePanelWidgetGroup setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public ScrollablePanelWidgetGroup setXBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.xBarB = background;
        this.xBarF = bar;
        return this;
    }

    public ScrollablePanelWidgetGroup setYBarStyle(IGuiTexture background, IGuiTexture bar) {
        this.yBarB = background;
        this.yBarF = bar;
        return this;
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
        computeMax();
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        computeMax();
    }

    protected void computeMax() {
        maxHeight = getSize().height;
        maxWidth = getSize().width;
        for (Widget widget : widgets) {
            maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getSelfPosition().y);
            maxWidth = Math.max(maxWidth, widget.getSize().width + widget.getSelfPosition().x);
        }
    }

    protected int getMaxHeight() {
        return maxHeight + xBarHeight;
    }

    protected int getMaxWidth() {
        return maxWidth + yBarWidth;
    }

    protected void setScrollXOffset(int scrollXOffset) {
        if (scrollXOffset == this.scrollXOffset) return;
        this.scrollXOffset = scrollXOffset;
        int minX = this.scrollXOffset + getPosition().x;
        int maxX = minX + getSize().width;
        for (Widget widget : widgets) {
            widget.setVisible(widget.getPosition().x < maxX && widget.getPosition().x + widget.getSize().width > minX);
        }
    }

    protected void setScrollYOffset(int scrollYOffset) {
        if (scrollYOffset == this.scrollYOffset) return;
        this.scrollYOffset = scrollYOffset;
        int minY = this.scrollYOffset + getPosition().y;
        int maxY = minY + getSize().height;
        for (Widget widget : widgets) {
            widget.setVisible(widget.getPosition().y < maxY && widget.getPosition().y + widget.getSize().height > minY);
        }
    }

    private boolean isOnXScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x, pos.y - xBarHeight, size.width, xBarHeight, mouseX, mouseY);
    }

    private boolean isOnYScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - yBarWidth, pos.y, yBarWidth, size.height, mouseX, mouseY);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(position.x, position.y, size.width, size.height);
        }
        GlStateManager.translate(-scrollXOffset, -scrollYOffset, 0);
        RenderUtil.useScissor(position.x, position.y, size.width - yBarWidth, size.height - xBarHeight, ()->{
            super.drawInBackground(mouseX +scrollXOffset, mouseY + scrollYOffset, partialTicks, context);
        });
        GlStateManager.translate(scrollXOffset, scrollYOffset, 0);
        if (xBarHeight > 0) {
            if (xBarB != null) {
                xBarB.draw(position.x, position.y - xBarHeight, size.width, xBarHeight);
            }
            if (xBarF != null) {
                int barWidth = size.width / getMaxWidth();
                xBarF.draw(position.x + scrollXOffset, position.y - xBarHeight, barWidth, xBarHeight);
            }
        }
        if (yBarWidth > 0) {
            if (yBarB != null) {
                yBarB.draw(position.x + size.width  - yBarWidth, position.y, yBarWidth, size.height);
            }
            if (yBarF != null) {
                int barHeight = (int) (size.height * 1.0f / getMaxHeight() * size.height);
                yBarF.draw(position.x + size.width  - yBarWidth, position.y + scrollYOffset * size.height * 1.0f / getMaxHeight(), yBarWidth, barHeight);
            }
        }

    }

    private void drawBar(Position position, Size size, int barSize, IGuiTexture barB, IGuiTexture barF, int anotherBarSize) {
        if (barSize > 0) {
            if (barB != null) {
                barB.draw(position.x, position.y - barSize, size.width, barSize);
            }
            if (barF != null) {
                int barWidth = (size.width + anotherBarSize) / getMaxWidth();
                barF.draw(position.x + scrollXOffset - barSize, position.y, barWidth, barSize);
            }
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        GlStateManager.translate(-scrollXOffset, -scrollYOffset, 0);
        super.drawInForeground(mouseX, mouseY + scrollYOffset);
        GlStateManager.translate(scrollXOffset, scrollYOffset, 0);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (xBarHeight > 0 && isOnXScrollPane(mouseX, mouseY)) {
            this.draggedOnXScrollBar = true;
            return true;
        }
        else if (yBarWidth > 0 && isOnYScrollPane(mouseX, mouseY)) {
            this.draggedOnYScrollBar = true;
            return true;
        } else if(isMouseOverElement(mouseX, mouseY)){
            if (super.mouseClicked(mouseX + scrollXOffset, mouseY + scrollYOffset, button)) {
                return true;
            } else if (draggable) {
                this.draggedPanel = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY, true)) {
            if (super.mouseWheelMove(mouseX + scrollXOffset, mouseY + scrollYOffset, wheelDelta)) {
                return true;
            }
            int moveDelta = -MathHelper.clamp(wheelDelta, -1, 1) * 5;
            if (getMaxHeight() - getSize().height > 0) {
                setScrollYOffset(MathHelper.clamp(scrollYOffset + moveDelta, 0, getMaxHeight() - getSize().height + 5));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (draggedOnXScrollBar) {
            setScrollXOffset(MathHelper.clamp(scrollXOffset + (mouseX - lastMouseX) * getMaxWidth() / getSize().width, 0, Math.max(getMaxWidth() - getSize().width, 0)));
        } else if (draggedOnYScrollBar) {
            setScrollYOffset(MathHelper.clamp(scrollYOffset + (mouseY - lastMouseY) * getMaxHeight() / getSize().height, 0, Math.max(getMaxHeight() - getSize().height, 0)));
        } else if (draggedPanel) {
            setScrollXOffset(MathHelper.clamp(scrollXOffset + lastMouseX - mouseX, 0, Math.max(getMaxWidth() - yBarWidth - getSize().width, 0)));
            setScrollYOffset(MathHelper.clamp(scrollYOffset + lastMouseY - mouseY, 0, Math.max(getMaxHeight() - xBarHeight - getSize().height, 0)));
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        return super.mouseDragged(mouseX + scrollXOffset, mouseY + scrollYOffset, button, timeDragged);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (draggedOnXScrollBar) {
            draggedOnXScrollBar = false;
        } else if (draggedOnYScrollBar) {
            draggedOnYScrollBar = false;
        } else if (draggedPanel) {
            draggedPanel = false;
        } else {
            return super.mouseReleased(mouseX + scrollXOffset, mouseY + scrollYOffset, button);
        }
        return true;
    }
}
