package gregtech.api.fluids.fluidType;

import gregtech.api.util.LocalizationUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTypeAcid extends FluidTypeLiquid {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_liquid";

    public FluidTypeAcid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    public String getUnlocalizedTooltip() {
        return TOOLTIP_NAME;
    }

    @Override
    public List<String> getAdditionalTooltips() {
        List<String> tooltips = super.getAdditionalTooltips();
        tooltips.add(LocalizationUtils.format("gregtech.fluid.type_acid.tooltip"));
        return tooltips;
    }
}
