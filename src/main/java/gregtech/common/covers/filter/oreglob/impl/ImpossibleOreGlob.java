package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobTextBuilder;

import javax.annotation.Nonnull;

/**
 * Simple implementation of oreglob that doesn't match anything.
 */
public final class ImpossibleOreGlob extends OreGlob {

    private static final ImpossibleOreGlob INSTANCE = new ImpossibleOreGlob();

    public static ImpossibleOreGlob getInstance() {
        return INSTANCE;
    }

    @Nonnull
    @Override
    public <V extends OreGlobTextBuilder> V visualize(@Nonnull V visualizer) {
        NodeVisualizer.impossible(visualizer);
        return visualizer;
    }

    @Override
    public boolean matches(@Nonnull String input) {
        return false;
    }
}
