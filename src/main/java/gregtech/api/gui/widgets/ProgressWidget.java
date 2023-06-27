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
        VERTICAL_INVERTED,
        CIRCULAR,
        VERTICAL_DOWNWARDS
    }

    public final DoubleSupplier progressSupplier;
    private MoveType moveType;
    private TextureArea emptyBarArea;
    private TextureArea[] filledBarArea;

    private double lastProgressValue;

    // TODO Clean up these constructors when Steam Machine UIs are cleaned up
    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
    }

    public ProgressWidget(int ticksPerCycle, int x, int y, int width, int height) {
        this(new TimedProgressSupplier(ticksPerCycle, width, false), x, y, width, height);
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.emptyBarArea = fullImage.getSubArea(0.0, 0.0, 1.0, 0.5);
        this.moveType = moveType;
        if (moveType == MoveType.CIRCULAR) {
            this.filledBarArea = new TextureArea[]{
                    fullImage.getSubArea(0.0, 0.75, 0.5, 0.25), // UP
                    fullImage.getSubArea(0.0, 0.5, 0.5, 0.25), // LEFT
                    fullImage.getSubArea(0.5, 0.5, 0.5, 0.25), // DOWN
                    fullImage.getSubArea(0.5, 0.75, 0.5, 0.25), // RIGHT
            };
        } else {
            this.filledBarArea = new TextureArea[]{fullImage.getSubArea(0.0, 0.5, 1.0, 0.5)};
        }
    }

    public ProgressWidget(int ticksPerCycle, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType) {
        this(new TimedProgressSupplier(
                ticksPerCycle,
                moveType == MoveType.HORIZONTAL ? width : height,
                moveType == MoveType.VERTICAL_INVERTED
        ), x, y, width, height, fullImage, moveType);
    }

    public ProgressWidget setProgressBar(TextureArea emptyBarArea, TextureArea filledBarArea, MoveType moveType) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = new TextureArea[]{filledBarArea};
        this.moveType = moveType;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (emptyBarArea != null) {
            emptyBarArea.draw(pos.x, pos.y, size.width, size.height);
        }
        if (filledBarArea != null) {
            //fuck this precision-dependent things, they are so fucking annoying
            if (moveType == MoveType.HORIZONTAL) {
                filledBarArea[0].drawSubArea(pos.x, pos.y, (int) (size.width * lastProgressValue), size.height,
                        0.0, 0.0, ((int) (size.width * lastProgressValue)) / (size.width * 1.0), 1.0);
            } else if (moveType == MoveType.VERTICAL) {
                int progressValueScaled = (int) (size.height * lastProgressValue);
                filledBarArea[0].drawSubArea(pos.x, pos.y + size.height - progressValueScaled, size.width, progressValueScaled,
                        0.0, 1.0 - (progressValueScaled / (size.height * 1.0)),
                        1.0, (progressValueScaled / (size.height * 1.0)));
            } else if (moveType == MoveType.VERTICAL_INVERTED) {
                int progressValueScaled = (int) (size.height * lastProgressValue);
                filledBarArea[0].drawSubArea(pos.x, pos.y, size.width, progressValueScaled,
                        0.0, 0.0,
                        1.0, (progressValueScaled / (size.height * 1.0)));
            } else if (moveType == MoveType.CIRCULAR) {
                double[] subAreas = new double[] {
                        Math.min(1, Math.max(0, lastProgressValue / 0.25)),
                        Math.min(1, Math.max(0, (lastProgressValue - 0.25) / 0.25)),
                        Math.min(1, Math.max(0, (lastProgressValue - 0.5) / 0.25)),
                        Math.min(1, Math.max(0, (lastProgressValue - 0.75) / 0.25)),
                };
                int halfWidth = size.width / 2;
                int halfHeight = size.height / 2;

                int progressScaled = (int) Math.round(subAreas[0] * halfHeight);
                filledBarArea[0].drawSubArea(
                        pos.x, pos.y + size.height - progressScaled,
                        halfWidth, progressScaled,
                        0.0, 1.0 - progressScaled / (halfHeight * 1.0),
                        1.0, progressScaled / (halfHeight * 1.0)
                ); // BL, draw UP

                progressScaled = (int) Math.round(subAreas[1] * halfWidth);
                filledBarArea[1].drawSubArea(
                        pos.x, pos.y,
                        progressScaled, halfHeight,
                        0.0, 0.0,
                        progressScaled / (halfWidth * 1.0), 1.0
                ); // TL, draw RIGHT

                progressScaled = (int) Math.round(subAreas[2] * halfHeight);
                filledBarArea[2].drawSubArea(
                        pos.x + halfWidth, pos.y,
                        halfWidth, progressScaled,
                        0.0, 0.0,
                        1.0, progressScaled / (halfHeight * 1.0)
                ); // TR, draw DOWN

                progressScaled = (int) Math.round(subAreas[3] * halfWidth);
                filledBarArea[3].drawSubArea(
                        pos.x + size.width - progressScaled, pos.y + halfHeight,
                        progressScaled, halfHeight,
                        1.0 - progressScaled / (halfWidth * 1.0), 0.0,
                        progressScaled / (halfWidth * 1.0), 1.0
                ); // BR, draw LEFT
            } else if (moveType == MoveType.VERTICAL_DOWNWARDS) {
                filledBarArea[0].drawSubArea(pos.x, pos.y, size.width, (int) (size.height * lastProgressValue),
                        0.0, 0.0, 1.0, ((int) (size.height * lastProgressValue)) / (size.height * 1.0));
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

    public static class TimedProgressSupplier implements DoubleSupplier {

        private final int msPerCycle;
        private final int maxValue;
        private final boolean countDown;
        private final long startTime;

        public TimedProgressSupplier(int ticksPerCycle, int maxValue, boolean countDown) {
            this.msPerCycle = ticksPerCycle * 50;
            this.maxValue = maxValue;
            this.countDown = countDown;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public double getAsDouble() {
            return calculateTime();
        }

        private double calculateTime() {
            long currentTime = System.currentTimeMillis();
            long msPassed = (currentTime - startTime) % msPerCycle;
            int currentValue = (int) Math.floorDiv(msPassed * (maxValue + 1), msPerCycle);
            if (countDown) {
                return (maxValue - currentValue) / (maxValue * 1.0);
            }
            return currentValue / (maxValue * 1.0);
        }
    }
}
