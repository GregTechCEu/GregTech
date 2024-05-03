package gregtech.integration.jei.utils;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.BiConsumer;

public class JeiInteractableText {

    private final int x;
    private final int y;
    private int color;
    private String currentText;
    private int textWidth;
    private TextClickAction textClickAction;
    private BiConsumer<String, List<String>> tooltipBuilder;
    private int state;

    public JeiInteractableText(int x, int y, String defaultText, int color, int baseState) {
        this.x = x;
        this.y = y;
        this.currentText = defaultText;
        this.textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(defaultText);
        this.color = color;
        this.state = baseState;
    }

    public void render(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(currentText, x, y, color);
    }

    public JeiInteractableText setTooltipBuilder(BiConsumer<String, List<String>> builder) {
        this.tooltipBuilder = builder;
        return this;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + textWidth && mouseY <= y + 10;
    }

    public void setCurrentText(String text) {
        this.currentText = text;
        this.textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(currentText);
    }

    public String getCurrentText() {
        return this.currentText;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public JeiInteractableText setClickAction(TextClickAction action) {
        this.textClickAction = action;
        return this;
    }

    public TextClickAction getTextClickAction() {
        return this.textClickAction;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public void buildTooltip(List<String> baseTooltip) {
        if (tooltipBuilder == null) return;
        tooltipBuilder.accept(currentText, baseTooltip);
    }

    @FunctionalInterface
    public interface TextClickAction {

        boolean click(Minecraft minecraft, JeiInteractableText text, int mouseX, int mouseY, int mouseButton);
    }
}
