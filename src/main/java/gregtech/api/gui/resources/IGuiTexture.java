package gregtech.api.gui.resources;

public interface IGuiTexture {

    void draw(double x, double y, int width, int height);

    default void updateTick() {}

    IGuiTexture EMPTY = (x, y, width, height) -> {};
}
