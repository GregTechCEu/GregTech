package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.function.Consumer;

public class ConfiguratorWidget extends WidgetGroup {
    protected String name;
    protected boolean canDefault;
    protected boolean isDefault;
    protected JsonObject config;

    private int nameWidth;
    private Consumer<String> onUpdated;

    public ConfiguratorWidget(int x, int y, JsonObject config, String name, boolean canDefault) {
        super(new Position(x, y));
        this.name = name;
        this.canDefault = canDefault;
        this.config = config;
        if (canDefault && config.get(name).isJsonNull()) {
            isDefault = true;
        }
        this.addWidget(new LabelWidget(0, 4, name, -1).setShadow(true));
        if (isClientSide()) {
            nameWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(name);
        }
    }

    public ConfiguratorWidget setOnUpdated(Consumer<String> onUpdated) {
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
            drawHoveringText(ItemStack.EMPTY, Collections.singletonList("default value"), 100, mouseX, mouseY);
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
