package gregtech.integration.jei.utils.render;

import net.minecraft.client.Minecraft;

import mezz.jei.api.gui.IDrawable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class CompositeDrawable implements IDrawable {

    private final int width;
    private final int height;
    private final List<RenderNode> steps;

    public CompositeDrawable(int width, int height, List<RenderNode> steps) {
        this.width = width;
        this.height = height;
        this.steps = steps;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(@NotNull Minecraft minecraft, int xOffset, int yOffset) {
        for (RenderNode step : steps) {
            step.draw(minecraft, xOffset, yOffset);
        }
    }

    public static Builder startBuilder(int height, int width) {
        return new Builder(height, width);
    }

    public static class Builder {

        private final int height;
        private final int width;
        private final List<RenderNode> steps = new LinkedList<>();

        public Builder(int height, int width) {
            this.height = height;
            this.width = width;
        }

        public Builder then(Runnable step) {
            return then(((minecraft, xOffset, yOffset) -> step.run()));
        }

        public Builder then(RenderNode step) {
            steps.add(step);
            return this;
        }

        public CompositeDrawable build() {
            return new CompositeDrawable(width, height, steps);
        }
    }

    @FunctionalInterface
    public interface RenderNode {

        void draw(@NotNull Minecraft minecraft, int xOffset, int yOffset);
    }
}
