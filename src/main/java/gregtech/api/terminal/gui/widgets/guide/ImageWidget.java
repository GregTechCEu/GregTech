package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.resources.URLTexture;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.guide.configurator.NumberConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.SelectorConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.StringConfigurator;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.function.Consumer;

public class ImageWidget extends GuideWidget{
    public final static String NAME = "image";
    //config
    public String form;
    public String source;
    public int width;
    public int height;

    public transient IGuiTexture image;

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = super.getTemplate(isFixed);
        template.addProperty("form", "item");
        template.addProperty("source", "minecraft:ender_pearl");
        template.addProperty("width", 50);
        template.addProperty("height", 50);
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate) {
        super.loadConfigurator(group, config, isFixed, needUpdate);
        group.addWidget(new SelectorConfigurator(5, group.getWidgetBottomHeight() + 5, config, "form", Arrays.asList("url", "item", "resource")).setOnUpdated(needUpdate));
        group.addWidget(new StringConfigurator(5, group.getWidgetBottomHeight() + 5, config, "source").setOnUpdated(needUpdate));
        group.addWidget(new NumberConfigurator(5, group.getWidgetBottomHeight() + 5, config, "width").setOnUpdated(needUpdate));
        group.addWidget(new NumberConfigurator(5, group.getWidgetBottomHeight() + 5, config, "height").setOnUpdated(needUpdate));
    }

    @Override
    public void updateScreen() {
        if (image != null) {
            image.updateTick();
        }
    }

    @Override
    protected Widget initStream() {
        int pageWidth = getSize().width;
        int x = getSelfPosition().x;
        int y = getSelfPosition().y;
        if (page != null) {
            x = page.getMargin();
            pageWidth = page.getPageWidth() - 2 * x;
        }
        this.setSelfPosition(new Position(x + (pageWidth - width) / 2, y));
        this.setSize(new Size(width, height));
        return initFixed();
    }

    @Override
    protected Widget initFixed() {
        width = getSize().width;
        height = getSize().height;
        switch (form) {
            case "url":
                image = new URLTexture(source);
                break;
            case "item":
                image = new ItemStackTexture(Item.getByNameOrId(source));
                break;
            case "resource":
                image = new TextureArea(new ResourceLocation(source), 0.0, 0.0, 1.0, 1.0);
                break;
        }
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (image != null) {
            super.drawInBackground(mouseX, mouseY, partialTicks,context);
            GlStateManager.color(1,1,1,1);
            Position position = getPosition();
            image.draw(position.x, position.y, getSize().width, getSize().height);
        }
    }
}
