package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.resources.URLTexture;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.Item;

public class ImageWidget extends GuideWidget{
    //config
    public String form;
    public String source;
    public int width;
    public int height;

    public IGuiTexture image;

    @Override
    public void updateScreen() {
        if (image != null) {
            image.updateTick();
        }
    }

    @Override
    protected Widget initStream(int x, int y, int pageWidth, JsonObject config) {
        this.setSelfPosition(new Position(x + (pageWidth - width) / 2, y));
        this.setSize(new Size(width, height));
        return super.initStream(x, y, pageWidth, config);
    }

    @Override
    protected Widget initFixed(int x, int y, int width, int height, JsonObject config) {
        switch (form) {
            case "url":
                image = new URLTexture(source);
                break;
            case "item":
                image = new ItemStackTexture(Item.getByNameOrId(source));
                break;
            case "resource":
                image = TextureArea.fullImage(source);
                break;
        }
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (image != null) {
            super.drawInBackground(mouseX, mouseY, partialTicks,context);
            Position position = getPosition();
            image.draw(position.x, position.y, width, height);
        }
    }
}
