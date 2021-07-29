package gregtech.api.terminal.gui.widgets.guide;


import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.URLTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import javafx.geometry.Pos;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class GuidePageWidget extends AbstractWidgetGroup {
    private IGuiTexture background;
    private int scrollYOffset;
    private int maxHeight;

    public GuidePageWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
    }

    public GuidePageWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    protected void setScrollYOffset(int scrollYOffset) {
        if (scrollYOffset == this.scrollYOffset) return;
        this.scrollYOffset = scrollYOffset;
        int minY = this.scrollYOffset + getPosition().y;
        int maxY = minY + getSize().height;
        for (Widget widget : widgets) {
            if (widget.getPosition().y < maxY && widget.getPosition().y + widget.getSize().height > minY) {
                widget.setVisible(true);
            }
        }
    }

    @Override
    public void addWidget(Widget widget) {
        maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getPosition().y);
        super.addWidget(widget);
        if (widget instanceof IGuideWidget) {
            ((IGuideWidget) widget).setPage(this);
        }
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY, true)) {
            int moveDelta = -MathHelper.clamp(wheelDelta, -1, 1) * 5;
            setScrollYOffset(MathHelper.clamp(scrollYOffset + moveDelta, 0, Math.max(maxHeight - getSize().height, 0)));
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(position.x, position.y, size.width, size.height);
        } else {
            gregtech.api.gui.resources.RenderUtil.renderRect(position.x, position.y, size.width, size.height, 0, 0xffffffff);
        }
        GlStateManager.translate(0, -scrollYOffset, 0);
        RenderUtil.useScissor(position.x, position.y, size.width, size.height, ()->{
            for (Widget widget : widgets) {
                if (widget.isVisible()) {
                    widget.drawInBackground(mouseX, mouseY, partialTicks, context);
                }
            }
            GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        });
        GlStateManager.translate(0, scrollYOffset, 0);
    }
}
