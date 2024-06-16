package gregtech.datafix;

import org.jetbrains.annotations.NotNull;

/**
 * Versions of GT data.
 */
public enum GTDataVersion {

    /**
     * Version of data before multiple MTE registries were possible
     */
    V0_PRE_MTE,
    /**
     * Version of data after multiple MTE registries were possible
     */
    V1_POST_MTE;

    static final @NotNull GTDataVersion @NotNull [] VALUES = values();

    /**
     * @return the current version of GT data
     */
    public static @NotNull GTDataVersion currentVersion() {
        return VALUES[VALUES.length - 1];
    }
}
