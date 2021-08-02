package gregtech.api.terminal.gui.widgets.guide.congiurator;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
        this.addWidget(new LabelWidget(0, 2, name, -1));
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
        if (canDefault && isMouseOver(x + nameWidth + 4, y + 4, 5, 5, mouseX, mouseY)) {
            drawHoveringText(ItemStack.EMPTY, Collections.singletonList("default the value"), 100, mouseX, mouseY);
        }
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        drawSolidRect(x, y, this.getSize().width, 1, -1);
        if (canDefault) {
            drawBorder(x + nameWidth + 4, y + 4, 5, 5, 0xff000000, 1);
            if (isDefault) {
                drawSolidRect(x + nameWidth + 5, y + 5, 3, 3, 0xff000000);
            }
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int x = getPosition().x;
        int y = getPosition().y;
        if (!isDefault && super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (canDefault && isMouseOver(x + nameWidth + 4, y + 4, 5, 5, mouseX, mouseY)) {
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
