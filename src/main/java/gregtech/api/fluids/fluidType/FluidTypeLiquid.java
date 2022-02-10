package gregtech.api.fluids.fluidType;

import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTypeLiquid extends FluidType {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_liquid";

    public FluidTypeLiquid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    protected void setFluidProperties(@Nonnull Fluid fluid) {
        fluid.setGaseous(false);
        fluid.setViscosity(1000);
    }

    @Override
    public String getUnlocalizedTooltip() {
        return TOOLTIP_NAME;
    }
}
