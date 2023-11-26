package gregtech.api.gui.resources;

import gregtech.api.util.GTUtility;

import net.minecraft.util.ResourceLocation;

public class SizedTextureArea extends TextureArea {

    public final double pixelImageWidth;
    public final double pixelImageHeight;

    public SizedTextureArea(ResourceLocation imageLocation, double offsetX, double offsetY, double width, double height,
                            double pixelImageWidth, double pixelImageHeight) {
        super(imageLocation, offsetX, offsetY, width, height);
        this.pixelImageWidth = pixelImageWidth;
        this.pixelImageHeight = pixelImageHeight;
    }

    @Override
    public SizedTextureArea getSubArea(double offsetX, double offsetY, double width, double height) {
        return new SizedTextureArea(imageLocation,
                this.offsetX + (imageWidth * offsetX),
                this.offsetY + (imageHeight * offsetY),
                this.imageWidth * width,
                this.imageHeight * height,
                this.pixelImageWidth * width,
                this.pixelImageHeight * height);
    }

    public static SizedTextureArea fullImage(String imageLocation, int imageWidth, int imageHeight) {
        return new SizedTextureArea(GTUtility.gregtechId(imageLocation), 0.0, 0.0, 1.0, 1.0, imageWidth, imageHeight);
    }

    public void drawHorizontalCutArea(int x, int y, int width, int height) {
        drawHorizontalCutSubArea(x, y, width, height, 0.0, 1.0);
    }

    public void drawVerticalCutArea(int x, int y, int width, int height) {
        drawVerticalCutSubArea(x, y, width, height, 0.0, 1.0);
    }

    public void drawHorizontalCutSubArea(int x, int y, int width, int height, double drawnV, double drawnHeight) {
        int half = width / 2;
        double drawnWidth = half / pixelImageWidth;
        drawSubArea(x, y, half, height, 0.0, drawnV, drawnWidth, drawnHeight);
        drawSubArea(x + half, y, width - half, height, 1.0 - drawnWidth, drawnV, drawnWidth, drawnHeight);
    }

    public void drawVerticalCutSubArea(int x, int y, int width, int height, double drawnU, double drawnWidth) {
        int half = height / 2;
        double drawnHeight = half / pixelImageHeight;
        drawSubArea(x, y, width, half, drawnU, 0.0, drawnWidth, drawnHeight);
        drawSubArea(x, y + half, width, height - half, drawnU, 1.0 - drawnHeight, drawnWidth, drawnHeight);
    }
}
