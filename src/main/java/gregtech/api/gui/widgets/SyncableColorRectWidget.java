package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

public class SyncableColorRectWidget extends Widget {

    protected Supplier<Integer> colorSupplier;
    protected int borderWidth;
    protected int borderColor;
    protected boolean drawCheckerboard;
    protected int checkerboardGridRows;
    protected int checkerboardGridColumns;
    private int color;

    public SyncableColorRectWidget(int x, int y, int width, int height, Supplier<Integer> colorSupplier) {
        super(x, y, width, height);
        this.colorSupplier = colorSupplier;
        this.borderWidth = 0;
        drawCheckerboard = false;
        checkerboardGridRows = 1;
        checkerboardGridColumns = 1;
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

    /**
     * Make sure the number of rows and columns evenly divides into the size of the non-border area
     */
    public SyncableColorRectWidget drawCheckerboard(int checkerboardGridColumns, int checkerboardGridRows) {
        this.drawCheckerboard = true;
        this.checkerboardGridColumns = checkerboardGridColumns;
        this.checkerboardGridRows = checkerboardGridRows;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        Size size = getSize();
        drawSolidRect(position.x, position.y, size.width, size.height, borderColor);
        if (drawCheckerboard) {
            int white = 0xFFFFFFFF;
            int grey = 0xFFBFBFBF;
            int columnWidth = (size.width - 2 * borderWidth) / checkerboardGridColumns;
            int rowHeight = (size.height - 2 * borderWidth) / checkerboardGridRows;
            boolean whiteGrey = false;
            for (int i = 0; i < checkerboardGridRows; i++) {
                for (int j = 0; j < checkerboardGridColumns; j++) {
                    drawSolidRect(position.x + borderWidth + i * columnWidth, position.y + borderWidth + j * rowHeight,
                            columnWidth, rowHeight, whiteGrey ? white : grey);
                    whiteGrey = !whiteGrey;
                }
                whiteGrey = !whiteGrey;
            }
        }
        drawSolidRect(position.x + borderWidth, position.y + borderWidth, size.width - 2 * borderWidth,
                size.height - 2 * borderWidth, color);
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
