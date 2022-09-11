package gregtech.api.fluids.fluidType;

import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTypePlasma extends FluidType {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_plasma";

    public FluidTypePlasma(@NotNull String name, @Nullable String prefix, @Nullable String suffix, @NotNull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    protected void setFluidProperties(@NotNull Fluid fluid) {
        fluid.setGaseous(true);
        fluid.setDensity(-100000);
        fluid.setViscosity(10);
        fluid.setLuminosity(15);
    }

    @Override
    public String getUnlocalizedTooltip() {
        return TOOLTIP_NAME;
    }
}
