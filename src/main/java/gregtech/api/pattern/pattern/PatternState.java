package gregtech.api.pattern.pattern;

import gregtech.api.pattern.PatternError;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class PatternState {

    public boolean formed = false;
    public boolean shouldUpdate = true;
    public PatternError error;
    public Map<String, Object> storage = new HashMap<>();
    protected EnumCheckState state;

    public void setError(PatternError error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

    public PatternError getError() {
        return error;
    }

    @ApiStatus.Internal
    public void setState(EnumCheckState state) {
        this.state = state;
    }

    public EnumCheckState getState() {
        return state;
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
         * The structure is invalid, regardless of cache.
         */
        INVALID;

        public boolean isValid() {
            return ordinal() < 2;
        }
    }
}
