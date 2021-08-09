package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;

import java.util.function.DoubleSupplier;

public class ProgressWidget extends Widget {

    public enum MoveType {
        VERTICAL,
        HORIZONTAL,
        VERTICAL_INVERTED
    }

    public final DoubleSupplier progressSupplier;
    private MoveType moveType;
    private TextureArea emptyBarArea;
    private TextureArea filledBarArea;

    private double lastProgressValue;

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.emptyBarArea = fullImage.getSubArea(0.0, 0.0, 1.0, 0.5);
        this.filledBarArea = fullImage.getSubArea(0.0, 0.5, 1.0, 0.5);
        this.moveType = moveType;
    }

    public ProgressWidget setProgressBar(TextureArea emptyBarArea, TextureArea filledBarArea, MoveType moveType) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = filledBarArea;
        this.moveType = moveType;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (emptyBarArea != null) {
            emptyBarArea.draw(pos.x, pos.y, size.width, size.height);
        }
        if (filledBarArea != null) {
            //fuck this precision-dependent things, they are so fucking annoying
            if (moveType == MoveType.HORIZONTAL) {
                filledBarArea.drawSubArea(pos.x, pos.y, (int) (size.width * lastProgressValue), size.height,
                        0.0, 0.0, ((int) (size.width * lastProgressValue)) / (size.width * 1.0), 1.0);
            } else if (moveType == MoveType.VERTICAL) {
                int progressValueScaled = (int) (size.height * lastProgressValue);
                filledBarArea.drawSubArea(pos.x, pos.y + size.height - progressValueScaled, size.width, progressValueScaled,
                        0.0, 1.0 - (progressValueScaled / (size.height * 1.0)),
                        1.0, (progressValueScaled / (size.height * 1.0)));
            } else if (moveType == MoveType.VERTICAL_INVERTED) {
                int progressValueScaled = (int) (size.height * lastProgressValue);
                filledBarArea.drawSubArea(pos.x, pos.y, size.width, progressValueScaled,
                        0.0, 0.0,
                        1.0, (progressValueScaled / (size.height * 1.0)));
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        double actualValue = progressSupplier.getAsDouble();
        if (Math.abs(actualValue - lastProgressValue) > 0.005) {
            this.lastProgressValue = actualValue;
            writeUpdateInfo(0, buffer -> buffer.writeDouble(actualValue));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastProgressValue = buffer.readDouble();
        }
    }
}
