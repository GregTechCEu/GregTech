package gregtech.common.terminal.app.guideeditor.widget.configurator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.function.Consumer;

public class ConfiguratorWidget<T> extends WidgetGroup {
    protected T defaultValue;
    protected String name;
    protected boolean canDefault;
    protected boolean isDefault;
    protected JsonObject config;
    protected DraggableScrollableWidgetGroup group;
    private int nameWidth;
    private Consumer<String> onUpdated;

    public ConfiguratorWidget(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        this(group, config, name, null);
    }

    public ConfiguratorWidget(DraggableScrollableWidgetGroup group, JsonObject config, String name, T defaultValue) {
        super(new Position(5, group.getWidgetBottomHeight() + 5));
        this.group = group;
        this.defaultValue = defaultValue;
        this.name = name;
        this.canDefault = defaultValue != null;
        this.config = config;
        if (config.get(name) == null) {
            config.addProperty(name, (String)null);
        }
        if (canDefault && config.get(name).isJsonNull()) {
            isDefault = true;
        }
        this.addWidget(new LabelWidget(0, 4, name, -1).setShadow(true));
        if (isClientSide()) {
            nameWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(name);
        }
        init();
    }

    protected void init() {

    }

    protected void updateValue(T value) {
        if (canDefault && isDefault) return;
        config.add(name, new Gson().toJsonTree(value));
        update();
    }

    public ConfiguratorWidget<T> setOnUpdated(Consumer<String> onUpdated) {
        this.onUpdated = onUpdated;
        return this;
    }

    protected void update(){
        if (onUpdated != null) {
            onUpdated.accept(name);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        int x = getPosition().x;
        int y = getPosition().y;
        if (canDefault && isMouseOver(x + nameWidth + 4, y + 6, 5, 5, mouseX, mouseY)) {
            drawHoveringText(ItemStack.EMPTY, Collections.singletonList(I18n.format("terminal.guide_editor.default")), 100, mouseX, mouseY);
        }
        if (!isDefault) {
            super.drawInForeground(mouseX, mouseY);
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        drawSolidRect(x, y, this.getSize().width, 1, -1);
        if (canDefault) {
            drawBorder(x + nameWidth + 4, y + 6, 5, 5, -1, 1);
            if (isDefault) {
                drawSolidRect(x + nameWidth + 5, y + 7, 3, 3, -1);
            }
        }
        if (canDefault && isDefault) {
            super.drawInBackground(-100, -100, partialTicks, context);
            drawSolidRect(x, y + 15, this.getSize().width, this.getSize().height - 15, 0x99000000);
        }  else {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
        }

    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int x = getPosition().x;
        int y = getPosition().y;
        if (!isDefault && super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (canDefault && isMouseOver(x + nameWidth + 4, y + 6, 5, 5, mouseX, mouseY)) {
            isDefault = !isDefault;
            if (isDefault) {
                config.addProperty(name, (String) null);
                update();
                onDefault();
            }
            playButtonClickSound();
            return true;
        }
        return false;
    }

    protected void onDefault() {
    }
}
