package gregtech.api.pattern.pattern;

import gregtech.api.pattern.PatternError;

import org.jetbrains.annotations.ApiStatus;

public class PatternState {

    protected boolean isFormed = false;

    /**
     * For use by the pattern to set its flipped state. This value has no meaning if the state is invalid.
     */
    protected boolean isFlipped = false;

    /**
     * For use by the multiblock to update its flipped state.
     */
    protected boolean actualFlipped = false;
    protected boolean shouldUpdate = false;
    /**
     * For if the multiblock manually invalidates its state(like coil mismatch). This means wait until the cache is
     * no longer valid and the raw check passes to reform the structure.
     */
    protected boolean isWaiting = false;
    protected PatternError error;
    protected EnumCheckState state;

    public boolean isFlipped() {
        return isFlipped;
    }

    @ApiStatus.Internal
    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public boolean isFormed() {
        return isFormed;
    }

    public void setFormed(boolean formed) {
        isFormed = formed;
    }

    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    protected void shouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
    }

    public void setError(PatternError error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

    protected void setState(EnumCheckState state) {
        this.state = state;
    }

    public EnumCheckState getState() {
        return state;
    }

    public void setActualFlipped(boolean actualFlipped) {
        this.actualFlipped = actualFlipped;
    }

    public boolean isActualFlipped() {
        return actualFlipped;
    }

    /**
     * Scuffed enum representing the result of the structure check.
     */
    public enum EnumCheckState {

        /**
         * The cache doesn't match with the structure's data. The structure has been rechecked from scratch, is valid,
         * and the cache is now populated.
         */
        VALID_UNCACHED,

        /**
         * The cache matches the structure's data.
         */
        VALID_CACHED,

        /**
         * The cache doesn't match with the structure's data. The structure has been rechecked from scratch, is invalid,
         * and the cache is now empty.
         */
        INVALID_CACHED,

        /**
         * The cache is empty. The structure has been rechecked from scratch and is invalid, the cache remains empty.
         */
        INVALID_UNCACHED;

        public boolean isValid() {
            return ordinal() < 2;
        }
    }
}
