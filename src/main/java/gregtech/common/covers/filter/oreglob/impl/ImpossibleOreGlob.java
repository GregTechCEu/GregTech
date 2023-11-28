package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobTextBuilder;

import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of oreglob that doesn't match anything.
 */
public final class ImpossibleOreGlob extends OreGlob {

    private static final ImpossibleOreGlob INSTANCE = new ImpossibleOreGlob();

    public static ImpossibleOreGlob getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public <V extends OreGlobTextBuilder> V visualize(@NotNull V visualizer) {
        NodeVisualizer.impossible(visualizer);
        return visualizer;
    }

    @Override
    public boolean matches(@NotNull String input) {
        return false;
    }
}
