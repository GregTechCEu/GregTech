package gregtech.api.unification.material.properties;

public class BlastProperty implements IMaterialProperty {

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     *
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
    public void verifyProperty(Properties properties) {
        if (properties.getIngotProperty() == null) {
            properties.setIngotProperty(new IngotProperty());
            properties.verify();
        }

        FluidProperty fluidProperty = properties.getFluidProperty();
        if (fluidProperty != null && fluidProperty.getFluidTemperature() == FluidProperty.BASE_TEMP)
            fluidProperty.setFluidTemperature(blastTemperature);
    }

    @Override
    public boolean doesMatch(IMaterialProperty otherProp) {
        return otherProp instanceof BlastProperty;
    }

    @Override
    public String getName() {
        return "blast_property";
    }

    @Override
    public String toString() {
        return getName();
    }
}
