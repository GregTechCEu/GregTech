package gregtech.integration.jei.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;

import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class CompositeRenderer<T> implements IIngredientRenderer<T> {

    private final TooltipSupplier<T> tooltipSupplier;
    private final FontRendererSupplier<T> fontRendererSupplier;
    private final List<RenderNode<T>> steps;

    public CompositeRenderer(TooltipSupplier<T> tooltipSupplier,
                             FontRendererSupplier<T> fontRendererSupplier,
                             List<RenderNode<T>> steps) {
        this.tooltipSupplier = tooltipSupplier;
        this.fontRendererSupplier = fontRendererSupplier;
        this.steps = steps;
    }

    public static <T> Builder<T> startBuilder(TooltipSupplier<T> tooltipSupplier,
                                              FontRendererSupplier<T> fontRendererSupplier) {
        return new Builder<>(tooltipSupplier, fontRendererSupplier);
    }

    public static <T> Builder<T> startBuilder(IIngredientRenderer<T> ingredientRenderer) {
        return startBuilder(ingredientRenderer::getTooltip,
                ingredientRenderer::getFontRenderer);
    }

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable T ingredient) {
        for (RenderNode<T> step : steps) {
            step.render(minecraft, xPosition, yPosition, ingredient);
        }
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, T ingredient, ITooltipFlag tooltipFlag) {
        return tooltipSupplier.getTooltip(minecraft, ingredient, tooltipFlag);
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, T ingredient) {
        return fontRendererSupplier.getFontRenderer(minecraft, ingredient);
    }

    public static class Builder<T> {

        private final TooltipSupplier<T> tooltipSupplier;
        private final FontRendererSupplier<T> fontRendererSupplier;
        private final List<RenderNode<T>> steps = new LinkedList<>();

        public Builder(TooltipSupplier<T> tooltipSupplier,
                       FontRendererSupplier<T> fontRendererSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            this.fontRendererSupplier = fontRendererSupplier;
        }

        public Builder<T> then(Runnable step) {
            return then(((minecraft, xPosition, yPosition, ingredient) -> step.run()));
        }

        public Builder<T> then(RenderNode<T> step) {
            steps.add(step);
            return this;
        }

        public CompositeRenderer<T> build() {
            return new CompositeRenderer<>(tooltipSupplier, fontRendererSupplier, steps);
        }
    }

    @FunctionalInterface
    public interface RenderNode<T> {

        void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable T ingredient);
    }

    @FunctionalInterface
    public interface TooltipSupplier<T> {

        List<String> getTooltip(Minecraft minecraft, T ingredient, ITooltipFlag tooltipFlag);
    }

    @FunctionalInterface
    public interface FontRendererSupplier<T> {

        FontRenderer getFontRenderer(Minecraft minecraft, T ingredient);
    }
}
