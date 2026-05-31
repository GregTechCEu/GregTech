package gregtech.api.color;

import net.minecraft.item.EnumDyeColor;

public enum ColorMode {

    /**
     * Only try spraying a block to an {@link EnumDyeColor}.
     */
    DYE(true, false),
    /**
     * Only try spraying a block to an ARGB value.
     */
    ARGB(false, true),
    /**
     * Try spraying the block to an {@link EnumDyeColor}, and if that failed fall back to ARGB.
     */
    PREFER_DYE(true, true),
    /**
     * Try spraying the block to an ARGB value, and if that failed fall back to {@link EnumDyeColor}.
     */
    PREFER_ARGB(true, true);

    private final boolean dye;
    private final boolean argb;

    ColorMode(boolean dye, boolean argb) {
        this.dye = dye;
        this.argb = argb;
    }

    public boolean dye() {
        return dye;
    }

    public boolean argb() {
        return argb;
    }
}
