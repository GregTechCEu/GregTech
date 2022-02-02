package gregtech.api.fluids.fluidType;

import javax.annotation.Nonnull;

public class FluidTypeAcid extends FluidTypeLiquid {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_liquid";

    public FluidTypeAcid(@Nonnull String name, @Nonnull String prefix, @Nonnull String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    public String getToolTipLocalization() {
        return TOOLTIP_NAME;
    }
}
