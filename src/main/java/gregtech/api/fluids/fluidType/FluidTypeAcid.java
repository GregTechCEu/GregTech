package gregtech.api.fluids.fluidType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTypeAcid extends FluidTypeLiquid {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_liquid";

    public FluidTypeAcid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    public String getToolTipLocalization() {
        return TOOLTIP_NAME;
    }
}
