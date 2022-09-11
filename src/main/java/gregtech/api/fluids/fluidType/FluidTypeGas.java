package gregtech.api.fluids.fluidType;

import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTypeGas extends FluidType {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_gas";

    public FluidTypeGas(@NotNull String name, @Nullable String prefix, @Nullable String suffix, @NotNull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    protected void setFluidProperties(@NotNull Fluid fluid) {
        fluid.setGaseous(true);
        fluid.setDensity(-100);
        fluid.setViscosity(200);
    }

    @Override
    public String getUnlocalizedTooltip() {
        return TOOLTIP_NAME;
    }
}
