package gregtech.api.worldgen.config;

import com.google.gson.JsonObject;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

public class BedrockFluidDepositDefinition implements IWorldgenDefinition { //todo re-balance depletion rates of default veins

    private final String depositName;

    private int weight; // weight value for determining which vein will appear
    private String assignedName; // vein name for JEI display
    private String description; // vein description for JEI display
    private final int[] productionRates = new int[2]; // the [minimum, maximum) production rate
    private int depletionAmount; // amount of fluid the vein gets drained by
    private int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    private int depletedProductionRate; // production rate after the vein is depleted

    private Fluid storedFluid; // the fluid which the vein contains

    private Function<Biome, Integer> biomeWeightModifier = OreDepositDefinition.NO_BIOME_INFLUENCE; // weighting of biomes
    private Predicate<WorldProvider> dimensionFilter = OreDepositDefinition.PREDICATE_SURFACE_WORLD; // filtering of dimensions

    public BedrockFluidDepositDefinition(String depositName) {
        this.depositName = depositName;
    }

    @Override
    public boolean initializeFromConfig(@Nonnull JsonObject configRoot) {
        // the weight value for determining which vein will appear
        this.weight = configRoot.get("weight").getAsInt();
        // the [minimum, maximum) production rate of the vein
        this.productionRates[0] = configRoot.get("rate").getAsJsonObject().get("min").getAsInt();
        this.productionRates[1] = configRoot.get("rate").getAsJsonObject().get("max").getAsInt();
        // amount of fluid the vein gets depleted by
        this.depletionAmount = configRoot.get("depletion").getAsJsonObject().get("amount").getAsInt();
        // the chance [0, 100] that the vein will deplete by depletionAmount
        this.depletionChance = Math.max(0, Math.min(100, configRoot.get("depletion").getAsJsonObject().get("chance").getAsInt()));

        // the fluid which the vein contains
        Fluid fluid = FluidRegistry.getFluid(configRoot.get("fluid").getAsString());
        if (fluid != null) {
            this.storedFluid = fluid;
        } else {
            GTLog.logger.error("Bedrock Fluid Vein {} cannot have a null fluid!", this.depositName, new RuntimeException());
            return false;
        }
        // vein name for JEI display
        if (configRoot.has("name")) {
            this.assignedName = configRoot.get("name").getAsString();
        }
        // vein description for JEI display
        if (configRoot.has("description")) {
            this.description = configRoot.get("description").getAsString();
        }
        // production rate after the vein is depleted
        if (configRoot.get("depletion").getAsJsonObject().has("depleted_production_rate")) {
            this.depletedProductionRate = configRoot.get("depletion").getAsJsonObject().get("depleted_production_rate").getAsInt();
        }
        // additional weighting changes determined by biomes
        if (configRoot.has("biome_modifier")) {
            this.biomeWeightModifier = WorldConfigUtils.createBiomeWeightModifier(configRoot.get("biome_modifier"));
        }
        // filtering of dimensions to determine where the vein can generate
        if (configRoot.has("dimension_filter")) {
            this.dimensionFilter = WorldConfigUtils.createWorldPredicate(configRoot.get("dimension_filter"));
        }
        BedrockFluidVeinHandler.addFluidDeposit(this);
        return true;
    }

    //This is the file name
    @Override
    public String getDepositName() {
        return depositName;
    }

    public String getAssignedName() {
        return assignedName;
    }

    public String getDescription() {
        return description;
    }

    public int getWeight() {
        return weight;
    }

    public int[] getProductionRates() {
        return productionRates;
    }

    public int getMinimumProductionRate() {
        return productionRates[0];
    }

    public int getMaximumProductionRate() {
        return productionRates[1];
    }

    public int getDepletionAmount() {
        return depletionAmount;
    }

    public int getDepletionChance() {
        return depletionChance;
    }

    public int getDepletedProductionRate() {
        return depletedProductionRate;
    }

    public Fluid getStoredFluid() {
        return storedFluid;
    }

    public Function<Biome, Integer> getBiomeWeightModifier() {
        return biomeWeightModifier;
    }

    public Predicate<WorldProvider> getDimensionFilter() {
        return dimensionFilter;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BedrockFluidDepositDefinition))
            return false;

        BedrockFluidDepositDefinition objDeposit = (BedrockFluidDepositDefinition) obj;
        if (this.weight != objDeposit.getWeight())
            return false;
        if (this.getMinimumProductionRate() != objDeposit.getMinimumProductionRate())
            return false;
        if (this.getMaximumProductionRate() != objDeposit.getMaximumProductionRate())
            return false;
        if (this.depletionAmount != objDeposit.getDepletionAmount())
            return false;
        if (this.depletionChance != objDeposit.getDepletionChance())
            return false;
        if (!this.storedFluid.equals(objDeposit.getStoredFluid()))
            return false;
        if ((this.assignedName == null && objDeposit.getAssignedName() != null) ||
                (this.assignedName != null && objDeposit.getAssignedName() == null) ||
                (this.assignedName != null && objDeposit.getAssignedName() != null && !this.assignedName.equals(objDeposit.getAssignedName())))
            return false;
        if ((this.description == null && objDeposit.getDescription() != null) ||
                (this.description != null && objDeposit.getDescription() == null) ||
                (this.description != null && objDeposit.getDescription() != null && !this.description.equals(objDeposit.getDescription())))
            return false;
        if (this.depletedProductionRate != objDeposit.getDepletedProductionRate())
            return false;
        if ((this.biomeWeightModifier == null && objDeposit.getBiomeWeightModifier() != null) ||
                (this.biomeWeightModifier != null && objDeposit.getBiomeWeightModifier() == null) ||
                (this.biomeWeightModifier != null && objDeposit.getBiomeWeightModifier() != null && !this.biomeWeightModifier.equals(objDeposit.getBiomeWeightModifier())))
            return false;
        if ((this.dimensionFilter == null && objDeposit.getDimensionFilter() != null) ||
                (this.dimensionFilter != null && objDeposit.getDimensionFilter() == null) ||
                (this.dimensionFilter != null && objDeposit.getDimensionFilter() != null && !this.dimensionFilter.equals(objDeposit.getDimensionFilter())))
            return false;

        return super.equals(obj);
    }
}
