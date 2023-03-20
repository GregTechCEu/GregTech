package gregtech.api.fluids.info;

import gregtech.api.unification.material.info.MaterialIconType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Possible states for a fluid
 */
public enum FluidState {
    /**
     * A regular liquid, like water
     */
    LIQUID,
    /**
     * A gas, like air
     */
    GAS,
    /**
     * A plama, like helium plasma
     */
    PLASMA;

    public static final FluidState[] VALUES = values();

    /**
     * @return the default icon type for the still texture for this state
     */
    @Nonnull
    public MaterialIconType getStillIconType() {
        switch (this) {
            case LIQUID: return MaterialIconType.liquid;
            case GAS: return MaterialIconType.gas;
            case PLASMA: return MaterialIconType.plasma;
            default: throw new IllegalStateException("FluidState " + this + " was in an impossible configuration");
        }
    }

    /**
     * @return the default icon type for the flowing texture for this state
     */
    @Nonnull
    public MaterialIconType getFlowingIconType() {
        switch (this) {
            case LIQUID: return MaterialIconType.liquid_flowing;
            case GAS: return MaterialIconType.gas;
            case PLASMA: return MaterialIconType.plasma;
            default: throw new IllegalStateException("FluidState " + this + " was in an impossible configuration");
        }
    }

    /**
     * @return the translation key for this state for a fluid tooltip
     */
    @Nonnull
    public String getTooltipTranslationKey() {
        switch (this) {
            case LIQUID: return "gregtech.fluid.state_liquid";
            case GAS: return  "gregtech.fluid.state_gas";
            case PLASMA: return  "gregtech.fluid.state_plasma";
            default: throw new IllegalStateException("FluidState " + this + " was in an impossible configuration");
        }
    }

    @Nullable
    public static FluidState getByName(@Nonnull String name) {
        for (FluidState state : VALUES) {
            if (state.name().equalsIgnoreCase(name)) {
                return state;
            }
        }
        return null;
    }
}
