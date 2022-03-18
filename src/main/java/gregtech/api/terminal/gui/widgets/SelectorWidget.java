package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectorWidget extends WidgetGroup {
    protected RectButtonWidget button;
    protected List<String> candidates;
    protected boolean isShow;
    private IGuiTexture background;
    private Consumer<String> onChanged;
    private Consumer<Boolean> onShowChange;
    private boolean isUp;
    private final int fontColor;

    public SelectorWidget(int x, int y, int width, int height, List<String> candidates, int fontColor, Supplier<String> supplier, boolean isClient) {
        super(new Position(x, y), new Size(width, height));
        this.button = new RectButtonWidget(0,0,width,height);
        this.candidates = candidates;
        this.fontColor = fontColor;
        button.setClickListener(d -> {
            if(onShowChange != null) {
                onShowChange.accept(!isShow);
            }
            isShow = !isShow;
        });
        this.addWidget(button);
        this.addWidget(new SimpleTextWidget(width / 2, height / 2, "", fontColor, supplier, isClient));
    }

    public SelectorWidget setIsUp(boolean isUp) {
        this.isUp = isUp;
        return this;
    }

    public SelectorWidget setHoverText(String text) {
        button.setHoverText(text);
        return this;
    }

    public SelectorWidget setOnChanged(Consumer<String> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public SelectorWidget setOnShowChange(Consumer<Boolean> onShowChange) {
        this.onShowChange = onShowChange;
        return this;
    }

    public SelectorWidget setColors(int stroke, int anima, int fill) {
        button.setColors(stroke, anima, fill);
        return this;
    }

    public SelectorWidget setButtonBackground(IGuiTexture guiTexture) {
        button.setIcon(guiTexture);
        return this;
    }

    public SelectorWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void hide() {
        this.isShow = false;
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if(isShow) {
            int x = getPosition().x;
            int width = getSize().width;
            int height = getSize().height;
            int y = (isUp ? -candidates.size() : 1) * height + getPosition().y;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            for (String candidate : candidates) {
                if (background != null) {
                    background.draw(x, y, width, height);
                } else {
                    drawSolidRect(x, y, width, height, 0xAA000000);
                }
                fontRenderer.drawString(I18n.format(candidate), x + 4, y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, fontColor);
                y += height;
            }
            y = (isUp ? -candidates.size() : 1) * height + getPosition().y;
            for (String ignored : candidates) {
                if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
                    drawBorder(x, y, width, height, -1, 1);
                }
                y += height;
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isShow) {
            int x = getPosition().x;
            int width = getSize().width;
            int height = getSize().height;
            int y = (isUp ? -candidates.size() : 1) * height + getPosition().y;
            for (String candidate : candidates) {
                if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
                    if (onChanged != null) {
                        onChanged.accept(candidate);
                    }
                    writeClientAction(2, buffer -> buffer.writeString(candidate));
                    isShow = false;
                    return true;
                }
                y += height;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
           if (onChanged != null) {
               onChanged.accept(buffer.readString(Short.MAX_VALUE));
           }
        }
    }
}
