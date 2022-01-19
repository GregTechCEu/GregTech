package gregtech.api.fluids.fluidType;

import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;

public class FluidTypeAcid extends FluidTypeLiquid {

    public FluidTypeAcid(@Nonnull String name, @Nonnull String prefix, @Nonnull String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    protected void setFluidProperties(@Nonnull Fluid fluid) {
        super.setFluidProperties(fluid);
    }
}
