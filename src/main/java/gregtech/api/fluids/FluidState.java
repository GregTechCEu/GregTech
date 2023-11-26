package gregtech.api.fluids;

import org.jetbrains.annotations.NotNull;

public enum FluidState {

    LIQUID("gregtech.fluid.state_liquid"),
    GAS("gregtech.fluid.state_gas"),
    PLASMA("gregtech.fluid.state_plasma");

    private final String translationKey;

    FluidState(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    public @NotNull String getTranslationKey() {
        return this.translationKey;
    }
}
