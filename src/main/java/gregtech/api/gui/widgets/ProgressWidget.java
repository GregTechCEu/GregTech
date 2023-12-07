package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public class ProgressWidget extends Widget {

    public enum MoveType {
        /** Fills the progress bar upwards, from the bottom */
        VERTICAL,
        /** Fills the progress bar left to right */
        HORIZONTAL,
        /** Progress bar starts full, and empties upwards from the bottom */
        VERTICAL_INVERTED,
        /** Fills the progress bar clockwise in a circle, starting from the bottom left */
        CIRCULAR,
        /** Fills the progress bar downwards, from the top */
        VERTICAL_DOWNWARDS,
        /** Fills the progress bar right to left */
        HORIZONTAL_BACKWARDS
    }

    public final DoubleSupplier progressSupplier;
    private MoveType moveType;
    private TextureArea emptyBarArea;
    private TextureArea[] filledBarArea;

    private double lastProgressValue;

    private List<ITextComponent> hoverText = new ArrayList<>();
    private Consumer<List<ITextComponent>> textSupplier;
    private boolean ignoreColor;

    // TODO Clean up these constructors when Steam Machine UIs are cleaned up
    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
    }

    public ProgressWidget(int ticksPerCycle, int x, int y, int width, int height) {
        this(new TimedProgressSupplier(ticksPerCycle, width, false), x, y, width, height);
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage,
                          MoveType moveType) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.emptyBarArea = fullImage.getSubArea(0.0, 0.0, 1.0, 0.5);
        this.moveType = moveType;
        if (moveType == MoveType.CIRCULAR) {
            this.filledBarArea = new TextureArea[] {
                    fullImage.getSubArea(0.0, 0.75, 0.5, 0.25), // UP
                    fullImage.getSubArea(0.0, 0.5, 0.5, 0.25), // LEFT
                    fullImage.getSubArea(0.5, 0.5, 0.5, 0.25), // DOWN
                    fullImage.getSubArea(0.5, 0.75, 0.5, 0.25), // RIGHT
            };
        } else {
            this.filledBarArea = new TextureArea[] { fullImage.getSubArea(0.0, 0.5, 1.0, 0.5) };
        }
    }

    public ProgressWidget(int ticksPerCycle, int x, int y, int width, int height, TextureArea fullImage,
                          MoveType moveType) {
        this(new TimedProgressSupplier(
                ticksPerCycle,
                moveType == MoveType.HORIZONTAL ? width : height,
                false), x, y, width, height, fullImage, moveType);
    }

    public ProgressWidget setProgressBar(TextureArea emptyBarArea, TextureArea filledBarArea, MoveType moveType) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = new TextureArea[] { filledBarArea };
        this.moveType = moveType;
        return this;
    }

    public ProgressWidget setHoverTextConsumer(Consumer<List<ITextComponent>> supplier) {
        this.textSupplier = supplier;
        return this;
    }

    public ProgressWidget setIgnoreColor(boolean ignore) {
        this.ignoreColor = ignore;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (ignoreColor) GlStateManager.color(1, 1, 1, 1);
        if (emptyBarArea != null) {
            emptyBarArea.draw(pos.x, pos.y, size.width, size.height);
        }
        if (filledBarArea != null) {
            final boolean smooth = ConfigHolder.client.guiConfig.smoothProgressBars;
            if (moveType == MoveType.HORIZONTAL) {
                double width = size.width * lastProgressValue;
                if (!smooth) width = (int) width;
                double drawnWidth = smooth ? lastProgressValue : width / (size.width * 1.0);
                filledBarArea[0].drawSubArea(
                        pos.x,
                        pos.y,
                        width,
                        size.height,
                        0.0,
                        0.0,
                        drawnWidth,
                        1.0);
            } else if (moveType == MoveType.HORIZONTAL_BACKWARDS) {
                double width = size.width * lastProgressValue;
                if (!smooth) width = (int) width;
                double drawnWidth = smooth ? lastProgressValue : width / (size.width * 1.0);
                filledBarArea[0].drawSubArea(
                        pos.x + size.width - width,
                        pos.y,
                        width,
                        size.height,
                        1.0 - drawnWidth,
                        0.0,
                        drawnWidth,
                        1.0);
            } else if (moveType == MoveType.VERTICAL) {
                double height = size.height * lastProgressValue;
                if (!smooth) height = (int) height;
                double drawnHeight = height / size.height;
                filledBarArea[0].drawSubArea(
                        pos.x,
                        pos.y + size.height - height,
                        size.width,
                        height,
                        0.0,
                        1.0 - drawnHeight,
                        1.0,
                        drawnHeight);
            } else if (moveType == MoveType.VERTICAL_INVERTED) {
                double height = size.height * (1.0 - lastProgressValue);
                if (!smooth) height = (int) height;
                double drawnHeight = height / size.height;
                filledBarArea[0].drawSubArea(
                        pos.x,
                        pos.y,
                        size.width,
                        height,
                        0.0,
                        0.0,
                        1.0,
                        drawnHeight);
            } else if (moveType == MoveType.CIRCULAR) {
                double[] subAreas = {
                        Math.min(1, Math.max(0, lastProgressValue / 0.25)),
                        Math.min(1, Math.max(0, (lastProgressValue - 0.25) / 0.25)),
                        Math.min(1, Math.max(0, (lastProgressValue - 0.5) / 0.25)),
                        Math.min(1, Math.max(0, (lastProgressValue - 0.75) / 0.25)),
                };

                int halfWidth = size.width / 2;
                int halfHeight = size.height / 2;

                double progressScaled = subAreas[0] * halfHeight;
                if (!smooth) progressScaled = Math.round(progressScaled);
                double progressScaledDrawnHeight = progressScaled / halfHeight;

                // BL, draw UP
                filledBarArea[0].drawSubArea(
                        pos.x,
                        pos.y + size.height - progressScaled,
                        halfWidth,
                        progressScaled,
                        0.0,
                        1.0 - progressScaledDrawnHeight,
                        1.0,
                        progressScaledDrawnHeight);

                // TL, draw RIGHT
                progressScaled = subAreas[1] * halfWidth;
                if (!smooth) progressScaled = Math.round(progressScaled);
                double progressScaledDrawnWidth = progressScaled / halfWidth;
                filledBarArea[1].drawSubArea(
                        pos.x,
                        pos.y,
                        progressScaled,
                        halfHeight,
                        0.0,
                        0.0,
                        progressScaledDrawnWidth,
                        1.0);

                // TR, draw DOWN
                progressScaled = subAreas[2] * halfWidth;
                if (!smooth) progressScaled = Math.round(progressScaled);
                progressScaledDrawnHeight = progressScaled / halfHeight;
                filledBarArea[2].drawSubArea(
                        pos.x + halfWidth,
                        pos.y,
                        halfWidth,
                        progressScaled,
                        0.0,
                        0.0,
                        1.0,
                        progressScaledDrawnHeight);

                // BR, draw LEFT
                progressScaled = subAreas[3] * halfWidth;
                if (!smooth) progressScaled = Math.round(progressScaled);
                progressScaledDrawnWidth = progressScaled / halfWidth;
                filledBarArea[3].drawSubArea(
                        pos.x + size.width - progressScaled,
                        pos.y + halfHeight,
                        progressScaled,
                        halfHeight,
                        1.0 - progressScaledDrawnWidth,
                        0.0,
                        progressScaledDrawnWidth,
                        1.0);
            } else if (moveType == MoveType.VERTICAL_DOWNWARDS) {
                double height = size.height * lastProgressValue;
                if (!smooth) height = (int) height;
                double drawnHeight = height / size.height;
                filledBarArea[0].drawSubArea(
                        pos.x,
                        pos.y,
                        size.width,
                        height,
                        0.0,
                        0.0,
                        1.0,
                        drawnHeight);
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

        if (textSupplier != null) {
            List<ITextComponent> textBuffer = new ArrayList<>();
            textSupplier.accept(textBuffer);
            if (!hoverText.equals(textBuffer)) {
                this.hoverText = textBuffer;
                writeUpdateInfo(1, buffer -> {
                    buffer.writeVarInt(hoverText.size());
                    for (ITextComponent textComponent : hoverText) {
                        buffer.writeString(ITextComponent.Serializer.componentToJson(textComponent));
                    }
                });
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastProgressValue = buffer.readDouble();
        } else if (id == 1) {
            this.hoverText.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String jsonText = buffer.readString(32767);
                this.hoverText.add(ITextComponent.Serializer.jsonToComponent(jsonText));
            }
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (isMouseOverElement(mouseX, mouseY) && hoverText != null && !hoverText.isEmpty()) {
            List<String> hoverList = new ArrayList<>();
            for (ITextComponent component : hoverText) {
                Collections.addAll(hoverList, component.getFormattedText());
            }
            if (!hoverList.isEmpty()) {
                drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
            }
        }
    }

    public static class TimedProgressSupplier implements DoubleSupplier {

        private final int msPerCycle;
        private final int maxValue;
        private final boolean countDown;
        private long startTime;

        public TimedProgressSupplier(int ticksPerCycle, int maxValue, boolean countDown) {
            this.msPerCycle = ticksPerCycle * 50;
            this.maxValue = maxValue;
            this.countDown = countDown;
            this.startTime = System.currentTimeMillis();
        }

        public void resetCountdown() {
            startTime = System.currentTimeMillis();
        }

        @Override
        public double getAsDouble() {
            return calculateTime();
        }

        private double calculateTime() {
            long currentTime = System.currentTimeMillis();
            long msPassed = (currentTime - startTime) % msPerCycle;
            double currentValue = 1.0 * msPassed * maxValue / msPerCycle;
            if (countDown) {
                return (maxValue - currentValue) / maxValue;
            }
            return currentValue / maxValue;
        }
    }
}
