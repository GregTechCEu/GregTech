package gregtech.api.pattern.pattern;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Simple wrapper class for a single substructure
 */
public class PatternInfo {

    @NotNull
    protected final IBlockPattern pattern;
    protected boolean isFormed = false;
    protected boolean isFlipped = false;
    public boolean shouldUpdate = true;

    public PatternInfo(@NotNull IBlockPattern pattern) {
        this.pattern = pattern;
    }

    public boolean isFormed() {
        return isFormed;
    }

    public void setFormed(boolean val) {
        this.isFormed = val;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    @ApiStatus.Internal
    public void setFlipped(boolean val) {
        isFlipped = val;
    }

    public @NotNull IBlockPattern getPattern() {
        return pattern;
    }
}
