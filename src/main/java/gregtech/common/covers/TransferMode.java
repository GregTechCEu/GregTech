package gregtech.common.covers;

import gregtech.api.util.ITranslatable;

import com.cleanroommc.modularui.api.widget.ITooltip;
import org.jetbrains.annotations.NotNull;

public enum TransferMode implements ITranslatable {

    TRANSFER_ANY("cover.%s.transfer_mode.transfer_any"),
    TRANSFER_EXACT("cover.%s.transfer_mode.transfer_exact"),
    KEEP_EXACT("cover.%s.transfer_mode.keep_exact"),
    RETAIN_EXACT("cover.%s.transfer_mode.retain_exact");

    public static final TransferMode[] VALUES = values();
    private final String localeName;

    TransferMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public @NotNull String getName() {
        throw new UnsupportedOperationException(
                "TransferMode#getName() called, this wouldn't produce any usable output, use the keyed getName instead!");
    }

    @Override
    public @NotNull String getName(@NotNull String key) {
        return String.format(localeName, key);
    }

    @Override
    public void handleTooltip(@NotNull ITooltip<?> tooltip, @NotNull String key) {
        tooltip.addTooltipLine(getName(key));
        tooltip.addTooltipLine(getName(key) + ".description");
    }
}
