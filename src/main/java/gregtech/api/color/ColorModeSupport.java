package gregtech.api.color;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.NotNull;

public enum ColorModeSupport {

    /**
     * This block only supports being colored to an {@link EnumDyeColor}.
     */
    DYE_ONLY("gregtech.color_mode.error.argb"),
    /**
     * This block only supports being colored to an ARGB value.
     */
    ARGB_ONLY("gregtech.color_mode.error.dye"),
    /**
     * This block supports being colored to a {@link EnumDyeColor} or ARGB value.
     */
    EITHER("gregtech.color_mode.error.either");

    @NotNull
    private final String errorKey;

    ColorModeSupport(@NotNull String errorKey) {
        this.errorKey = errorKey;
    }

    public @NotNull String getErrorTranslationKey() {
        return errorKey;
    }

    public @NotNull IKey getErrorKey() {
        return IKey.lang(errorKey);
    }

    public @NotNull ITextComponent getErrorText() {
        return new TextComponentTranslation(errorKey);
    }

    public boolean supportsMode(@NotNull ColorMode colorMode) {
        return switch (this) {
            case DYE_ONLY -> colorMode.dye();
            case ARGB_ONLY -> colorMode.argb();
            case EITHER -> true;
        };
    }
}
