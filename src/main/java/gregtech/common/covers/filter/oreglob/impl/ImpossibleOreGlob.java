package gregtech.common.covers.filter.oreglob.impl;

import gregtech.api.util.oreglob.OreGlob;

import static gregtech.api.util.oreglob.OreGlob.VisualizationHint.LOGIC_INVERSION;

/**
 * 'Empty' implementation of oreglob. This instance doesn't match anything.
 */
public final class ImpossibleOreGlob extends OreGlob {

    private static final ImpossibleOreGlob INSTANCE = new ImpossibleOreGlob();

    public static ImpossibleOreGlob getInstance() {
        return INSTANCE;
    }

    @Override
    public <V extends Visualizer> V visualize(V visualizer) {
        visualizer.text("(impossible to match)", LOGIC_INVERSION);
        return visualizer;
    }

    @Override
    public boolean matches(String input) {
        return input.isEmpty();
    }
}
