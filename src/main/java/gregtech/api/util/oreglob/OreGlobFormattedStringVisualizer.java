package gregtech.api.util.oreglob;

import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of oreGlob visualizer that outputs list of formatted strings as result.
 */
public class OreGlobFormattedStringVisualizer extends OreGlobVisualizer {

    public OreGlobFormattedStringVisualizer() {}

    public OreGlobFormattedStringVisualizer(@Nonnull String indent) {
        super(indent);
    }

    @Nullable
    @Override
    public TextFormatting getColor(@Nonnull VisualizationHint hint) {
        return switch (hint) {
            case TEXT, LABEL -> TextFormatting.GRAY;
            case NODE -> TextFormatting.WHITE;
            case VALUE -> TextFormatting.YELLOW;
            case LOGIC -> TextFormatting.GOLD;
            case NOT, ERROR -> TextFormatting.RED;
        };
    }
}
