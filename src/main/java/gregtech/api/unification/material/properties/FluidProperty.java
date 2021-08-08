package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;
import crafttweaker.api.liquid.ILiquidDefinition;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;

//@ZenClass("mods.gregtech.material.FluidMaterial")
//@ZenRegister
public class FluidProperty implements IMaterialProperty<FluidProperty> {

    public static final int BASE_TEMP = 300;

    /**
     * Internal material fluid field
     */
    private Fluid fluid;

    private boolean hasBlock;
    private final boolean isGas;
    private int fluidTemperature = BASE_TEMP;

    public FluidProperty(boolean isGas, boolean hasBlock) {
        this.isGas = isGas;
        this.hasBlock = hasBlock;
    }

    /**
     * Default values of: no Block, not Gas.
     */
    public FluidProperty() {
        this(false, false);
    }

    //@ZenGetter("hasFluid")
    public boolean shouldGenerateFluid() {
        return true;
    }

    //@ZenGetter("isGaseous")
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

    @Nonnull
    public FluidStack getFluid(int amount) {
        return new FluidStack(fluid, amount);
    }

    //@ZenMethod("setFluidTemperature")
    public void setFluidTemperature(int fluidTemperature) {
        Preconditions.checkArgument(fluidTemperature > 0, "Invalid temperature");
        this.fluidTemperature = fluidTemperature;
    }

    //@ZenGetter("fluidTemperature")
    public int getFluidTemperature() {
        return fluidTemperature;
    }

    //@ZenGetter("fluid")
    @Optional.Method(modid = GTValues.MODID_CT)
    @Nonnull
    public ILiquidDefinition ctGetFluid() {
        return CraftTweakerMC.getILiquidDefinition(fluid);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.PLASMA)) {
            hasBlock = false;
        }
    }
}
