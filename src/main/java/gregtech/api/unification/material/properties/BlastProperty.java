package gregtech.api.unification.material.properties;

public class BlastProperty implements IMaterialProperty<BlastProperty> {

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature
     * will be set to this if it is the default Fluid temperature.
     */
    private final int blastTemperature;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    /**
     * Default property constructor.
     */
    public BlastProperty() {
        this(0);
    }

    public int getBlastTemperature() {
        return blastTemperature;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);

        FluidProperty fluidProperty = properties.getProperty(PropertyKey.FLUID);
        if (fluidProperty != null && fluidProperty.getFluidTemperature() == FluidProperty.BASE_TEMP)
            fluidProperty.setFluidTemperature(blastTemperature);
    }
}
