package gregtech.api.terminal.app;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import javafx.geometry.Pos;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

public abstract class AbstractApplication extends WidgetGroup {
    protected Interpolator interpolator;
    protected final String name;
    protected final IGuiTexture icon;
    private float scale;

    public AbstractApplication (String name, IGuiTexture icon) {
        super(Position.ORIGIN, new Size(333, 232));
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public IGuiTexture getIcon() {
        return icon;
    }

    public abstract AbstractApplication createApp(boolean isClient, NBTTagCompound nbt);

    public void maximizeApp(Consumer<AbstractApplication> callback) {
        this.scale = 0;
        setVisible(true);
        interpolator = new Interpolator(0, 1, 10, Eases.EaseLinear,
                value-> scale = value.floatValue(),
                value-> {
                    interpolator = null;
                    if (callback != null) {
                        callback.accept(this);
                    }
                });
        interpolator.start();
    }

    public void minimizeApp(Consumer<AbstractApplication> callback) {
        this.scale = 1;
        interpolator = new Interpolator(1, 0, 10, Eases.EaseLinear,
                value-> scale = value.floatValue(),
                value-> {
                    setVisible(false);
                    interpolator = null;
                    if (callback != null) {
                        callback.accept(this);
                    }
                });
        interpolator.start();
    }

    public void closeApp(boolean isClient, NBTTagCompound nbt) {
    }

    public boolean isBackgroundApp() {
        return false;
    }

    @Override
    public void updateScreen() {
        if (interpolator != null) {
            interpolator.update();
        }
        super.updateScreen();
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (scale == 0) {
            return;
        } if (scale != 1) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((this.gui.getScreenWidth() - this.gui.getScreenWidth() * scale) / 2,
                    (this.gui.getScreenHeight() - this.gui.getScreenHeight() * scale) / 2, 0);
            GlStateManager.scale(scale, scale, 1);
            super.drawInForeground(0, 0);
            GlStateManager.popMatrix();
        } else {
            super.drawInForeground(mouseX, mouseY);
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (scale == 0) {
            return;
        }if (scale != 1) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((this.gui.getScreenWidth() - this.gui.getScreenWidth() * scale) / 2,
                    (this.gui.getScreenHeight() - this.gui.getScreenHeight() * scale) / 2, 0);
            GlStateManager.scale(scale, scale, 1);
            super.drawInBackground(0, 0, partialTicks, context);
            GlStateManager.popMatrix();
        } else {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
        }

    }
}
