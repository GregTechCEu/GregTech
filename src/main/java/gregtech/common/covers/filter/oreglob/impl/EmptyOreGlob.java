package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;

import static gregtech.api.util.oreglob.OreGlob.VisualizationHint.LOGIC_INVERSION;

/**
 * 'Empty' implementation of oreglob. This instance only matches empty string.
 */
public final class EmptyOreGlob extends OreGlob {

    private static final EmptyOreGlob INSTANCE = new EmptyOreGlob();

    public static EmptyOreGlob getInstance() {
        return INSTANCE;
    }

    @Override
    public <V extends Visualizer> V visualize(V visualizer) {
        visualizer.text("nothing", LOGIC_INVERSION);
        return visualizer;
    }

    @Override
    public boolean matches(String input) {
        return input.isEmpty();
    }
}
