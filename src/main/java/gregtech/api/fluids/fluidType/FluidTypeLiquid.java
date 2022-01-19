package gregtech.api.fluids.fluidType;

import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;

public class FluidTypeLiquid extends FluidType {

    public FluidTypeLiquid(@Nonnull String name, @Nonnull String prefix, @Nonnull String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    protected void setFluidProperties(@Nonnull Fluid fluid) {
        fluid.setGaseous(false);
        fluid.setViscosity(1000);
    }
}
