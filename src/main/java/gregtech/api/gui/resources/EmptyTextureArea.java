package gregtech.api.gui.resources;

public class EmptyTextureArea extends TextureArea {
    public EmptyTextureArea(double width, double height) {
        super(null, 0, 0, width, height);
    }

    @Override
    public TextureArea getSubArea(double offsetX, double offsetY, double width, double height) {
        return new EmptyTextureArea(width, height);
    }

    @Override
    public void draw(double x, double y, int width, int height) {
    }

    @Override
    public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) {
    }
}
