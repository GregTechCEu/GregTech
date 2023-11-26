package gregtech.api.gui.resources;

import gregtech.api.util.GTUtility;

import net.minecraft.util.ResourceLocation;

public class AdoptableTextureArea extends SizedTextureArea {

    private final int pixelCornerWidth;
    private final int pixelCornerHeight;

    public AdoptableTextureArea(ResourceLocation imageLocation, double offsetX, double offsetY, double width,
                                double height, double pixelImageWidth, double pixelImageHeight, int pixelCornerWidth,
                                int pixelCornerHeight) {
        super(imageLocation, offsetX, offsetY, width, height, pixelImageWidth, pixelImageHeight);
        this.pixelCornerWidth = pixelCornerWidth;
        this.pixelCornerHeight = pixelCornerHeight;
    }

    public static AdoptableTextureArea fullImage(String imageLocation, int imageWidth, int imageHeight, int cornerWidth,
                                                 int cornerHeight) {
        return new AdoptableTextureArea(GTUtility.gregtechId(imageLocation), 0.0, 0.0, 1.0, 1.0, imageWidth,
                imageHeight, cornerWidth, cornerHeight);
    }

    @Override
    public void drawSubArea(double x, double y, double width, double height, double drawnU, double drawnV,
                            double drawnWidth, double drawnHeight) {
        // compute relative sizes
        double cornerWidth = pixelCornerWidth / pixelImageWidth;
        double cornerHeight = pixelCornerHeight / pixelImageHeight;
        // draw up corners
        super.drawSubArea(x, y, pixelCornerWidth, pixelCornerHeight, 0.0, 0.0, cornerWidth, cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y, pixelCornerWidth, pixelCornerHeight, 1.0 - cornerWidth, 0.0,
                cornerWidth, cornerHeight);
        // draw down corners
        super.drawSubArea(x, y + height - pixelCornerHeight, pixelCornerWidth, pixelCornerHeight, 0.0,
                1.0 - cornerHeight, cornerWidth, cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y + height - pixelCornerHeight, pixelCornerWidth,
                pixelCornerHeight, 1.0 - cornerWidth, 1.0 - cornerHeight, cornerWidth, cornerHeight);
        // draw horizontal connections
        super.drawSubArea(x + pixelCornerWidth, y, width - 2 * pixelCornerWidth, pixelCornerHeight,
                cornerWidth, 0.0, 1.0 - 2 * cornerWidth, cornerHeight);
        super.drawSubArea(x + pixelCornerWidth, y + height - pixelCornerHeight, width - 2 * pixelCornerWidth,
                pixelCornerHeight,
                cornerWidth, 1.0 - cornerHeight, 1.0 - 2 * cornerWidth, cornerHeight);
        // draw vertical connections
        super.drawSubArea(x, y + pixelCornerHeight, pixelCornerWidth, height - 2 * pixelCornerHeight,
                0.0, cornerHeight, cornerWidth, 1.0 - 2 * cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y + pixelCornerHeight, pixelCornerWidth,
                height - 2 * pixelCornerHeight,
                1.0 - cornerWidth, cornerHeight, cornerWidth, 1.0 - 2 * cornerHeight);
        // draw central body
        super.drawSubArea(x + pixelCornerWidth, y + pixelCornerHeight,
                width - 2 * pixelCornerWidth, height - 2 * pixelCornerHeight,
                cornerWidth, cornerHeight, 1.0 - 2 * cornerWidth, 1.0 - 2 * cornerHeight);
    }
}
