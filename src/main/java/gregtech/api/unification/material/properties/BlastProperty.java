package gregtech.api.unification.material.properties;

import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraftforge.fluids.FluidStack;

import crafttweaker.CraftTweakerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlastProperty implements IMaterialProperty {

    /**
     * Blast Furnace Temperature of this Material. If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature will be set to this if it is the default Fluid
     * temperature.
     */
    private int blastTemperature;

    /**
     * The {@link GasTier} of this Material, representing which Gas EBF recipes will be generated.
     * <p>
     * Default: null, meaning no Gas EBF recipes.
     */
    private GasTier gasTier = null;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getAverageMass() * blastTemperature / 50
     */
    private int durationOverride = -1;

    /**
     * The EU/t of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120.
     */
    private int eutOverride = -1;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getMass() * 3
     */
    private int vacuumDurationOverride = -1;

    /**
     * The EU/t of the Vacuum Freezer recipe (if needed), overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120 EU/t.
     */
    private int vacuumEUtOverride = -1;

    /**
     * A {@link FluidStack} to use as the input when auto-generating the freezer recipe.
     * Default: null, meaning to either use none or liquid helium if the blast temperature > 4999k.
     */
    private FluidStack freezerFluidInput = null;

    /**
     * A {@link FluidStack} to use as the output when auto-generating the freezer recipe.
     * Default: null, meaning to either use none or liquid helium if the blast temperature > 4999k.
     */
    private FluidStack freezerFluidOutput = null;

    /**
     * Whether to automatically generate a vacuum freezer recipe.
     * Default: true
     */
    private boolean generateFreezerRecipe = true;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
    }

    private BlastProperty(int blastTemperature, GasTier gasTier, int eutOverride, int durationOverride,
                          int vacuumEUtOverride, int vacuumDurationOverride, @Nullable FluidStack freezerFluidInput,
                          @Nullable FluidStack freezerFluidOutput, boolean generateFreezerRecipe) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
        this.eutOverride = eutOverride;
        this.durationOverride = durationOverride;
        this.vacuumEUtOverride = vacuumEUtOverride;
        this.vacuumDurationOverride = vacuumDurationOverride;
        this.freezerFluidInput = freezerFluidInput;
        this.freezerFluidOutput = freezerFluidOutput;
        this.generateFreezerRecipe = generateFreezerRecipe;
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

    public void setGasTier(@NotNull GasTier tier) {
        this.gasTier = tier;
    }

    public int getDurationOverride() {
        return durationOverride;
    }

    public void setDurationOverride(int duration) {
        this.durationOverride = duration;
    }

    public int getEUtOverride() {
        return eutOverride;
    }

    public void setEutOverride(int eut) {
        this.eutOverride = eut;
    }

    public int getVacuumDurationOverride() {
        return vacuumDurationOverride;
    }

    public void setVacuumDurationOverride(int duration) {
        this.vacuumDurationOverride = duration;
    }

    public int getVacuumEUtOverride() {
        return vacuumEUtOverride;
    }

    public void setVacuumEutOverride(int eut) {
        this.vacuumEUtOverride = eut;
    }

    public @Nullable FluidStack getFreezerFluidInput() {
        return this.freezerFluidInput;
    }

    public void setFreezerFluidInput(FluidStack freezerFluidInput) {
        this.freezerFluidInput = freezerFluidInput;
    }

    public @Nullable FluidStack getFreezerFluidOutput() {
        return this.freezerFluidOutput;
    }

    public void setFreezerFluidOutput(FluidStack freezerFluidOutput) {
        this.freezerFluidInput = freezerFluidOutput;
    }

    public boolean generatesFreezerRecipe() {
        return this.generateFreezerRecipe;
    }

    public void generatesFreezerRecipe(boolean generateFreezerRecipe) {
        this.generateFreezerRecipe = generateFreezerRecipe;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    public static GasTier validateGasTier(String gasTierName) {
        if (gasTierName == null) return null;
        if (GroovyScriptModule.isCurrentlyRunning()) {
            return GroovyScriptModule.parseAndValidateEnumValue(GasTier.class, gasTierName, "gas tier");
        }
        if ("LOW".equalsIgnoreCase(gasTierName)) return GasTier.LOW;
        else if ("MID".equalsIgnoreCase(gasTierName)) return GasTier.MID;
        else if ("HIGH".equalsIgnoreCase(gasTierName)) return GasTier.HIGH;
        else if ("HIGHER".equalsIgnoreCase(gasTierName)) return GasTier.HIGHER;
        else if ("HIGHEST".equalsIgnoreCase(gasTierName)) return GasTier.HIGHEST;
        else {
            String message = "Gas Tier must be either \"LOW\", \"MID\", \"HIGH\", \"HIGHER\", or \"HIGHEST\"";
            CraftTweakerAPI.logError(message);
            throw new IllegalArgumentException(
                    "Could not find valid gas tier for name: " + gasTierName + ". " + message);
        }
    }

    public enum GasTier {

        // Tiers used by GTCEu
        LOW,
        MID,
        HIGH,

        // Tiers reserved for addons
        HIGHER,
        HIGHEST;

        public static final GasTier[] VALUES = values();
    }

    public static class Builder {

        private int temp;
        private GasTier gasTier;
        private int eutOverride = -1;
        private int durationOverride = -1;
        private int vacuumEUtOverride = -1;
        private int vacuumDurationOverride = -1;
        private FluidStack freezerFluidInput, freezerFluidOutput = null;
        private boolean generateFreezerRecipe = true;

        public Builder() {}

        public Builder temp(int temperature) {
            this.temp = temperature;
            return this;
        }

        public Builder temp(int temperature, GasTier gasTier) {
            this.temp = temperature;
            this.gasTier = gasTier;
            return this;
        }

        public Builder blastStats(int eutOverride) {
            this.eutOverride = eutOverride;
            return this;
        }

        public Builder blastStats(int eutOverride, int durationOverride) {
            this.eutOverride = eutOverride;
            this.durationOverride = durationOverride;
            return this;
        }

        public Builder vacuumStats(int eutOverride) {
            this.vacuumEUtOverride = eutOverride;
            return this;
        }

        public Builder vacuumStats(int eutOverride, int durationOverride) {
            this.vacuumEUtOverride = eutOverride;
            this.vacuumDurationOverride = durationOverride;
            return this;
        }

        public Builder freezerFluidOverride(@Nullable FluidStack input, @Nullable FluidStack output) {
            this.freezerFluidInput = input;
            this.freezerFluidOutput = output;
            return this;
        }

        public Builder freezerFluidInputOverride(@NotNull FluidStack input) {
            this.freezerFluidInput = input;
            return this;
        }

        public Builder freezerFluidOutputOverride(@NotNull FluidStack output) {
            this.freezerFluidOutput = output;
            return this;
        }

        public Builder disableFreezerRecipe() {
            this.generateFreezerRecipe = false;
            return this;
        }

        public BlastProperty build() {
            return new BlastProperty(temp, gasTier, eutOverride, durationOverride, vacuumEUtOverride,
                    vacuumDurationOverride, freezerFluidInput, freezerFluidOutput, generateFreezerRecipe);
        }
    }
}
