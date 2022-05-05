package gregtech.api.gui.widgets;

import com.google.common.base.Preconditions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClickButtonWidget extends Widget {

    protected TextureArea buttonTexture = GuiTextures.VANILLA_BUTTON.getSubArea(0.0, 0.0, 1.0, 0.5);
    protected final String displayText;
    protected int textColor = 0xFFFFFF;
    protected final Consumer<ClickData> onPressCallback;
    protected boolean shouldClientCallback;
    protected Supplier<Boolean> shouldDisplay;

    private String tooltipText;
    private Object[] tooltipArgs;

    public ClickButtonWidget(int xPosition, int yPosition, int width, int height, String displayText, Consumer<ClickData> onPressed) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.displayText = displayText;
        this.onPressCallback = onPressed;
        this.shouldDisplay = () -> true;
    }

    public ClickButtonWidget setShouldClientCallback(boolean shouldClientCallback) {
        this.shouldClientCallback = shouldClientCallback;
        return this;
    }

    public ClickButtonWidget setButtonTexture(TextureArea buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public ClickButtonWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public ClickButtonWidget setDisplayFunction(Supplier<Boolean> displayFunction) {
        this.shouldDisplay = displayFunction;
        return this;
    }

    public ClickButtonWidget setTooltipText(String tooltipText, Object... args) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        this.tooltipArgs = args;
        return this;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && shouldDisplay.get();
    }

    @Override
    public boolean isActive() {
         return super.isActive() && shouldDisplay.get();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        if (!shouldDisplay.get()) return;
        Position position = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(position.x, position.y, size.width, size.height, 0.0, 1.0);
        } else {
            buttonTexture.drawSubArea(position.x, position.y, size.width, size.height, 0.0, 0.0, 1.0, 1.0);
        }
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String text = I18n.format(displayText);
        fontRenderer.drawString(text,
                position.x + size.width / 2 - fontRenderer.getStringWidth(text) / 2,
                position.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, textColor);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (tooltipText != null && isMouseOverElement(mouseX, mouseY)) {
            List<String> hoverList = Arrays.asList(I18n.format(tooltipText, tooltipArgs).split("/n"));
            drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!shouldDisplay.get()) return false;
        if (isMouseOverElement(mouseX, mouseY)) {
            triggerButton();
            return true;
        }
        return false;
    }

    protected void triggerButton() {
        ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown());
        writeClientAction(1, clickData::writeToBuf);
        if (shouldClientCallback) {
           onPressCallback.accept(new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), true));
        }
        playButtonClickSound();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            onPressCallback.accept(clickData);
        }
    }
}
