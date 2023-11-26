package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public abstract class AnimaWidgetGroup extends WidgetGroup {

    @SideOnly(Side.CLIENT)
    protected Interpolator interpolator;
    protected float scale = 1;

    public AnimaWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public AnimaWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    @Override
    public void updateScreenOnFrame() {
        if (interpolator != null) {
            interpolator.update();
        }
        super.updateScreenOnFrame();
    }

    @SideOnly(Side.CLIENT)
    public final void maximizeWidget(Consumer<AnimaWidgetGroup> callback) {
        this.scale = 0;
        setVisible(true);
        interpolator = new Interpolator(0, 1, 10, Eases.LINEAR,
                value -> scale = value.floatValue(),
                value -> {
                    interpolator = null;
                    if (callback != null) {
                        callback.accept(this);
                    }
                });
        interpolator.start();
    }

    @SideOnly(Side.CLIENT)
    public final void minimizeWidget(Consumer<AnimaWidgetGroup> callback) {
        this.scale = 1;
        interpolator = new Interpolator(1, 0, 10, Eases.LINEAR,
                value -> scale = value.floatValue(),
                value -> {
                    setVisible(false);
                    interpolator = null;
                    if (callback != null) {
                        callback.accept(this);
                    }
                });
        interpolator.start();
    }

    @SideOnly(Side.CLIENT)
    protected void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @SideOnly(Side.CLIENT)
    protected void hookDrawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    public final void drawInForeground(int mouseX, int mouseY) {
        if (scale == 0) {
            return;
        }
        if (scale != 1) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((this.gui.getScreenWidth() - this.gui.getScreenWidth() * scale) / 2,
                    (this.gui.getScreenHeight() - this.gui.getScreenHeight() * scale) / 2, 0);
            GlStateManager.scale(scale, scale, 1);
            hookDrawInForeground(0, 0);
            GlStateManager.popMatrix();
        } else {
            hookDrawInForeground(mouseX, mouseY);
        }
    }

    @Override
    public final void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (scale == 0) {
            return;
        }
        if (scale != 1) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((this.gui.getScreenWidth() - this.gui.getScreenWidth() * scale) / 2,
                    (this.gui.getScreenHeight() - this.gui.getScreenHeight() * scale) / 2, 0);
            GlStateManager.scale(scale, scale, 1);
            hookDrawInBackground(0, 0, partialTicks, context);
            GlStateManager.popMatrix();
        } else {
            hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        }
    }
}
