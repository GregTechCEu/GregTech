package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;

/**
 * Simple implementation of oreglob that doesn't match anything.
 */
public final class ImpossibleOreGlob extends OreGlob {

    private static final ImpossibleOreGlob INSTANCE = new ImpossibleOreGlob();

    public static ImpossibleOreGlob getInstance() {
        return INSTANCE;
    }

    @Override
    public <V extends Visualizer> V visualize(V visualizer) {
        NodeVisualizer.impossible(visualizer);
        return visualizer;
    }

    @Override
    public boolean matches(String input) {
        return false;
    }
}
