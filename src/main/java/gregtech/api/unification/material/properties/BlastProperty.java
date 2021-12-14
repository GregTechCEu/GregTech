package gregtech.api.unification.material.properties;

import crafttweaker.CraftTweakerAPI;

public class BlastProperty implements IMaterialProperty<BlastProperty> {

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature
     * will be set to this if it is the default Fluid temperature.
     */
    private int blastTemperature;

    /**
     * The {@link GasTier} of this Material, representing which Gas EBF recipes will be generated.
     *
     * Default: null, meaning no Gas EBF recipes.
     */
    private GasTier gasTier = null;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     *
     * Default: -1, meaning the duration will be: material.getAverageMass() * blastTemperature / 50
     */
    private int durationOverride = -1;

    /**
     * The EU/t of the EBF recipe, overriding the stock behavior.
     *
     * Default: -1, meaning the EU/t will be 120.
     */
    private int eutOverride = -1;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier, int eutOverride, int durationOverride) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
        this.eutOverride = eutOverride;
        this.durationOverride = durationOverride;
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

    public void setBlastTemperature(int blastTemp) {
        if (blastTemp <= 0) throw new IllegalArgumentException("Blast Temperature must be greater than zero!");
        this.blastTemperature = blastTemp;
    }

    public GasTier getGasTier() {
        return gasTier;
    }

    public int getDurationOverride() {
        return durationOverride;
    }

    public int getEUtOverride() {
        return eutOverride;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);

        FluidProperty fluidProperty = properties.getProperty(PropertyKey.FLUID);
        if (fluidProperty != null && fluidProperty.getFluidTemperature() == FluidProperty.BASE_TEMP)
            fluidProperty.setFluidTemperature(blastTemperature);
    }

    public static GasTier validateGasTier(String gasTierName) {
        if (gasTierName == null) return null;
        else if (gasTierName.equalsIgnoreCase("LOW")) return GasTier.LOW;
        else if (gasTierName.equalsIgnoreCase("MID")) return GasTier.MID;
        else if (gasTierName.equalsIgnoreCase("HIGH")) return GasTier.HIGH;
        else if (gasTierName.equalsIgnoreCase("HIGHER")) return GasTier.HIGHER;
        else if (gasTierName.equalsIgnoreCase("HIGHEST")) return GasTier.HIGHEST;
        else {
            CraftTweakerAPI.logError("Gas Tier must be either \"LOW\", \"MID\", \"HIGH\", \"HIGHER\", or \"HIGHEST\"");
            throw new IllegalArgumentException();
        }
    }

    public enum GasTier {
        // Tiers used by GTCEu
        LOW, MID, HIGH,

        // Tiers reserved for addons
        HIGHER, HIGHEST;

        public static final GasTier[] VALUES = values();
    }
}
