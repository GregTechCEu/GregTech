package gregtech.api.util.oreglob;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Formatting function for OreGlob visualization.
 */
@FunctionalInterface
public interface OreGlobTextFormatting {

    /**
     * Plaintext formatting.
     */
    OreGlobTextFormatting NO_FORMATTING = h -> null;
    /**
     * Default color scheme for tooltip display.
     */
    OreGlobTextFormatting DEFAULT_FORMATTING = h -> switch (h) {
        case TEXT, LABEL -> TextFormatting.GRAY;
        case NODE -> TextFormatting.WHITE;
        case VALUE -> TextFormatting.YELLOW;
        case LOGIC -> TextFormatting.GOLD;
        case NOT, ERROR -> TextFormatting.RED;
    };

    @Nullable
    TextFormatting getFormat(@NotNull VisualizationHint hint);
}
