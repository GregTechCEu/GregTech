package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class SyncableColorRectWidget extends Widget {

    protected Supplier<Integer> colorSupplier;
    protected int borderWidth;
    protected int borderColor;
    private int color;

    public SyncableColorRectWidget(int x, int y, int width, int height, Supplier<Integer> colorSupplier) {
        super(x, y, width, height);
        this.colorSupplier = colorSupplier;
        this.borderWidth = 0;
        borderColor = 0xFF000000;
    }

    public SyncableColorRectWidget setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public SyncableColorRectWidget setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        Size size = getSize();
        //GTLog.logger.info(position.x + ":" + position.y + ":" + size.width + ":" + size.height + ":" + borderWidth + "/" + borderColor + ":" + color);
        drawSolidRect(position.x, position.y, size.width, size.height, borderColor);
        drawSolidRect(position.x + borderWidth, position.y + borderWidth, size.width - 2*borderWidth, size.height - 2*borderWidth, color);
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (colorSupplier != null && colorSupplier.get() != color) {
            this.color = colorSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeInt(color));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.color = buffer.readInt();
        }
    }
}
