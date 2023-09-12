package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.fluids.store.FluidStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class FluidProperty implements IMaterialProperty {

    public static final int BASE_TEMP = 293; // Room Temperature

    private final FluidStorage storage = new FluidStorage();

    /**
     * Internal material fluid field
     */
    private Fluid fluid;


    private boolean hasBlock;
    private boolean isGas;
    private int fluidTemperature = BASE_TEMP;

    public FluidProperty() {}

    public FluidProperty(@Nonnull FluidType fluidType, boolean hasBlock) {
        this.isGas = fluidType == FluidTypes.GAS;
        this.hasBlock = hasBlock;
    }

    public @NotNull FluidStorage getStorage() {
        return this.storage;
    }

    @Deprecated
    public boolean isGas() {
        return isGas;
    }

    /**
     * internal usage only
     */
    @Deprecated
    public void setFluid(@Nonnull Fluid materialFluid) {
        Preconditions.checkNotNull(materialFluid);
        this.fluid = materialFluid;
    }

    @Deprecated
    public Fluid getFluid() {
        return fluid;
    }

    @Deprecated
    public boolean hasBlock() {
        return hasBlock;
    }

    @Deprecated
    public void setHasBlock(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }

    @Deprecated
    public void setIsGas(boolean isGas) {
        this.isGas = isGas;
    }

    @Nonnull
    public FluidStack getFluid(int amount) {
        return new FluidStack(fluid, amount);
    }

    @Deprecated
    public void setFluidTemperature(int fluidTemperature) {
        setFluidTemperature(fluidTemperature, true);
    }

    @Deprecated
    public void setFluidTemperature(int fluidTemperature, boolean isKelvin) {
        if (isKelvin) Preconditions.checkArgument(fluidTemperature >= 0, "Invalid temperature");
        else fluidTemperature += 273;
        this.fluidTemperature = fluidTemperature;
        if (fluid != null)
            fluid.setTemperature(fluidTemperature);
    }

    @Deprecated
    public int getFluidTemperature() {
        return fluidTemperature;
    }

    @Deprecated
    @Nonnull
    public FluidType getFluidType() {
        return null;
    }

    @Deprecated
    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
