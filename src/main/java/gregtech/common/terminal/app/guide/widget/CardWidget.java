package gregtech.common.terminal.app.guide.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.terminal.app.guideeditor.widget.configurator.BooleanConfigurator;
import gregtech.common.terminal.app.guideeditor.widget.configurator.NumberConfigurator;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class CardWidget extends GuideWidget {

    public final static String NAME = "card";

    // config
    public int width;
    public int height;
    public boolean isShadow;

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = super.getTemplate(isFixed);
        template.addProperty("fill", -3745585);
        template.addProperty("width", 120);
        template.addProperty("height", 60);
        template.addProperty("isShadow", true);
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed,
                                 Consumer<String> needUpdate) {
        group.addWidget(new BooleanConfigurator(group, config, "isShadow", true).setOnUpdated(needUpdate));
        if (!isFixed) {
            group.addWidget(new NumberConfigurator(group, config, "width").setOnUpdated(needUpdate));
            group.addWidget(new NumberConfigurator(group, config, "height").setOnUpdated(needUpdate));
        }
        super.loadConfigurator(group, config, isFixed, needUpdate);
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
        return initFixed();
    }

    @Override
    protected Widget initFixed() {
        this.setSize(new Size(width, height));
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (isShadow) {
            int x = getPosition().x;
            int y = getPosition().y;
            int width = getSize().width;
            int height = getSize().height;
            drawRectShadow(x, y, width, height, 5);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
