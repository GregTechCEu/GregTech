package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidProperty implements IMaterialProperty<FluidProperty> {

    public static final int BASE_TEMP = 293; // Room Temperature

    /**
     * Internal material fluid field
     */
    private Fluid fluid;

    private final FluidType fluidType;

    private boolean hasBlock;
    private boolean isGas;
    private int fluidTemperature = BASE_TEMP;

    public FluidProperty(@Nonnull FluidType fluidType, boolean hasBlock) {
        this.fluidType = fluidType;
        this.isGas = fluidType == FluidTypes.GAS;
        this.hasBlock = hasBlock;
    }

    /**
     * Default values of: no Block, not Gas.
     */
    public FluidProperty() {
        this(FluidTypes.LIQUID, false);
    }

    public boolean isGas() {
        return isGas;
    }

    /**
     * internal usage only
     */
    public void setFluid(@Nonnull Fluid materialFluid) {
        Preconditions.checkNotNull(materialFluid);
        this.fluid = materialFluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public boolean hasBlock() {
        return hasBlock;
    }

    public void setHasBlock(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }

    public void setIsGas(boolean isGas) {
        this.isGas = isGas;
    }

    @Nonnull
    public FluidStack getFluid(int amount) {
        return new FluidStack(fluid, amount);
    }

    public void setFluidTemperature(int fluidTemperature) {
        setFluidTemperature(fluidTemperature, true);
    }

    public void setFluidTemperature(int fluidTemperature, boolean isKelvin) {
        if (isKelvin) Preconditions.checkArgument(fluidTemperature >= 0, "Invalid temperature");
        else fluidTemperature += 273;
        this.fluidTemperature = fluidTemperature;
        if (fluid != null)
            fluid.setTemperature(fluidTemperature);
    }

    public int getFluidTemperature() {
        return fluidTemperature;
    }

    @Nonnull
    public FluidType getFluidType() {
        return this.fluidType;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.PLASMA)) {
            hasBlock = false;
        }
    }
}
