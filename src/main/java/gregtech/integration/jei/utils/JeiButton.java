package gregtech.integration.jei.utils;

import gregtech.api.gui.resources.IGuiTexture;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class JeiButton {

    private IGuiTexture[] textures = {};
    private final float x;
    private final float y;
    private final int width;
    private final int height;
    private ClickAction clickAction;
    private BooleanSupplier activeSupplier = () -> true;
    private Consumer<List<String>> tooltipBuilder = null;

    public JeiButton(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public JeiButton setTextures(IGuiTexture... textures) {
        this.textures = textures;
        return this;
    }

    public JeiButton setClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public JeiButton setActiveSupplier(BooleanSupplier activeSupplier) {
        this.activeSupplier = activeSupplier;
        return this;
    }

    public JeiButton setTooltipBuilder(Consumer<List<String>> tooltipBuilder) {
        this.tooltipBuilder = tooltipBuilder;
        return this;
    }

    public void buildTooltip(List<String> lines) {
        if (tooltipBuilder != null) {
            tooltipBuilder.accept(lines);
        }
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height &&
                activeSupplier.getAsBoolean();
    }

    public void render(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (!activeSupplier.getAsBoolean())
            return;
        for (IGuiTexture texture : textures) {
            texture.draw(x, y, width, height);
        }
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    @FunctionalInterface
    public interface ClickAction {

        boolean click(Minecraft minecraft, int mouseX, int mouseY, int mouseButton);
    }
}
