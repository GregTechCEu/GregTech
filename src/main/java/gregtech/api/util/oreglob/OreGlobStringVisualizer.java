package gregtech.api.util.oreglob;

import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of oreGlob visualizer that outputs String as result.
 */
public class OreGlobStringVisualizer extends OreGlobVisualizer {

    public OreGlobStringVisualizer() {}

    public OreGlobStringVisualizer(@Nonnull String indent) {
        super(indent);
    }

    @Nullable
    @Override
    public TextFormatting getColor(@Nonnull VisualizationHint hint) {
        return null;
    }
}
