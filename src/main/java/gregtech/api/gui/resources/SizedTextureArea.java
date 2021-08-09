package gregtech.api.gui.resources;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;

public class SizedTextureArea extends TextureArea {

    public final double pixelImageWidth;
    public final double pixelImageHeight;

    public SizedTextureArea(ResourceLocation imageLocation, double offsetX, double offsetY, double width, double height, double pixelImageWidth, double pixelImageHeight) {
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
        return new SizedTextureArea(new ResourceLocation(GTValues.MODID, imageLocation), 0.0, 0.0, 1.0, 1.0, imageWidth, imageHeight);
    }

    public void drawHorizontalCutArea(int x, int y, int width, int height) {
        drawHorizontalCutSubArea(x, y, width, height, 0.0, 1.0);
    }

    public void drawVerticalCutArea(int x, int y, int width, int height) {
        drawVerticalCutSubArea(x, y, width, height, 0.0, 1.0);
    }

    public void drawHorizontalCutSubArea(int x, int y, int width, int height, double drawnV, double drawnHeight) {
        double drawnWidth = width / 2.0 / pixelImageWidth;
        drawSubArea(x, y, width / 2, height, 0.0, drawnV, drawnWidth, drawnHeight);
        drawSubArea(x + width / 2, y, width / 2, height, 1.0 - drawnWidth, drawnV, drawnWidth, drawnHeight);
    }

    public void drawVerticalCutSubArea(int x, int y, int width, int height, double drawnU, double drawnWidth) {
        double drawnHeight = height / 2.0 / pixelImageHeight;
        drawSubArea(x, y, width, height / 2, drawnU, 0.0, drawnWidth, drawnHeight);
        drawSubArea(x, y + height / 2, width, height / 2, drawnU, 1.0 - drawnHeight, drawnWidth, drawnHeight);
    }

}
