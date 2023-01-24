package gregtech.api.fluids;

import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidTag;
import gregtech.api.util.GTUtility;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * An advanced fluid with additional capabilities within the GregTech fluid-handling ecosystem
 */
public class AdvancedFluid extends Fluid implements IAdvancedFluid {

    private final FluidDefinition definition;

    /**
     * <strong>Do not construct this outside of a definition's {@code constructFluid} method.</strong>
     * @param name        the name of the fluid
     * @param color       an RGB color value for the fluid
     * @param definition  the definition for the fluid
     */
    public AdvancedFluid(@Nonnull String name, int color, @Nonnull FluidDefinition definition) {
        super(name, definition.getStill(), definition.getFlowing(), GTUtility.convertRGBtoOpaqueRGBA_MC(color));
        this.definition = definition;
        this.temperature = definition.getTemperature();
        this.isGaseous = definition.getState() == FluidState.GAS;
        switch (definition.getState()) {
            case LIQUID: {
                this.viscosity = 1000;
                break;
            }
            case GAS: {
                this.viscosity = 200;
                this.density = -100;
                break;
            }
            case PLASMA: {
                this.viscosity = 10;
                this.density = -100_000;
                this.luminosity = 15;
            }
        }
    }

    @Override
    public String getUnlocalizedName() {
        return this.definition.getTranslationKey();
    }

    @Nonnull
    @Override
    public FluidState getState() {
        return this.definition.getState();
    }

    @Nonnull
    @Override
    public Collection<FluidTag> getData() {
        return this.definition.getData();
    }
}
