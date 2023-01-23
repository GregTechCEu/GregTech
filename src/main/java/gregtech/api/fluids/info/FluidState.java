package gregtech.api.fluids.info;

import gregtech.api.unification.material.info.MaterialIconType;

import javax.annotation.Nonnull;

/**
 * Possible states for a fluid
 */
public enum FluidState {
    LIQUID,
    GAS,
    PLASMA;

    @Nonnull
    public MaterialIconType getStillIconType() {
        switch (this) {
            case LIQUID: return MaterialIconType.liquid;
            case GAS: return MaterialIconType.gas;
            case PLASMA: return MaterialIconType.plasma;
            default: throw new IllegalStateException("FluidState " + this + " was in an impossible configuration");
        }
    }

    @Nonnull
    public MaterialIconType getFlowingIconType() {
        switch (this) {
            case LIQUID: return MaterialIconType.liquid_flowing;
            case GAS: return MaterialIconType.gas;
            case PLASMA: return MaterialIconType.plasma;
            default: throw new IllegalStateException("FluidState " + this + " was in an impossible configuration");
        }
    }

    @Nonnull
    public String getTooltipTranslationKey() {
        switch (this) {
            case LIQUID: return "gregtech.fluid.state_liquid";
            case GAS: return  "gregtech.fluid.state_gas";
            case PLASMA: return  "gregtech.fluid.state_plasma";
            default: throw new IllegalStateException("FluidState " + this + " was in an impossible configuration");
        }
    }
}
