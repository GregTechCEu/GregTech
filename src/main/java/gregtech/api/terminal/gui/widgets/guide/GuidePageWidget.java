package gregtech.api.terminal.gui.widgets.guide;


import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.URLTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import javafx.geometry.Pos;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class GuidePageWidget extends AbstractWidgetGroup {
    private IGuiTexture background;
    private int scrollYOffset;
    private int maxHeight;
    private Interpolator interpolator;

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
            widget.setVisible(widget.getPosition().y < maxY && widget.getPosition().y + widget.getSize().height > minY);
        }
    }

    public int getScrollYOffset() {
        return scrollYOffset;
    }

    @Override
    public void updateScreen() {
        if (interpolator != null) interpolator.update();
        super.updateScreen();
    }

    public void jumpToRef(String ref){
        if (interpolator != null && !interpolator.isFinish()) return;
        for (Widget widget : widgets) {
            if (widget instanceof IGuideWidget && ref.equals(((IGuideWidget) widget).getRef())) {
                interpolator = new Interpolator(scrollYOffset, widget.getSelfPosition().y, 20, Eases.EaseQuadOut, (value)->{
                    setScrollYOffset(value.intValue());
                });
                interpolator.start();
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
            int offsetY = mouseY + scrollYOffset;
            for (Widget widget : widgets) {
                if (widget.isVisible()) {
                    widget.drawInBackground(mouseX, offsetY, partialTicks, context);
                }
            }
            GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        });
        GlStateManager.translate(0, scrollYOffset, 0);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        GlStateManager.translate(0, -scrollYOffset, 0);
        super.drawInForeground(mouseX, mouseY + scrollYOffset);
        GlStateManager.translate(0, scrollYOffset, 0);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY + scrollYOffset, button);
    }
}
