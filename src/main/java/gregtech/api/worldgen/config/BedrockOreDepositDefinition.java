package gregtech.api.worldgen.config;

import com.google.gson.JsonObject;

import crafttweaker.api.item.IItemStack;

import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.ore.StoneTypes;
import gregtech.api.util.GTLog;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;

import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class BedrockOreDepositDefinition implements IWorldgenDefinition {


    private final String depositName;
    private int weight; // weight value for determining which vein will appear
    private final int[] densities = new int[2]; // the minimum & maximum density [0, 100]
    private int depletionAmount; // the speed the vein gets drained by
    private int depletionChance; // the chance [0, 100] that the vein will deplete per operation
    private String assignedName; // vein name for JEI display
    private String description; // vein description for JEI display

    private StoneType stoneType;

    private Map<ItemStack, Integer> storedOres;

    private Function<Biome, Integer> biomeWeightModifier = OreDepositDefinition.NO_BIOME_INFLUENCE; // weighting of
    // biomes
    private Predicate<WorldProvider> dimensionFilter = OreDepositDefinition.PREDICATE_SURFACE_WORLD; // filtering of
    // dimensions

    public BedrockOreDepositDefinition(String depositName) {
        this.depositName = depositName;
    }

    @Override
    public boolean initializeFromConfig(@NotNull JsonObject configRoot) {
        // the weight value for determining which vein will appear
        this.weight = configRoot.get("weight").getAsInt();
        // the [minimum, maximum) density of the vein
        this.densities[0] = Math.max(0,
                Math.min(100, configRoot.get("densities").getAsJsonObject().get("min").getAsInt()));
        this.densities[1] = Math.max(this.densities[0],
                Math.min(100, configRoot.get("densities").getAsJsonObject().get("max").getAsInt()));
        // amount of operations the vein gets depleted by
        this.depletionAmount = configRoot.get("depletion").getAsJsonObject().get("amount").getAsInt();
        // the chance [0, 100] that the vein will deplete by depletionAmount
        this.depletionChance = Math.max(0,
                Math.min(100, configRoot.get("depletion").getAsJsonObject().get("chance").getAsInt()));

        if (configRoot.has("stone_type")) {
            this.stoneType = StoneType.STONE_TYPE_REGISTRY.registryObjects.get(
                    configRoot.get("stone_type").getAsJsonObject().get("amount").getAsString());
        }
        if (stoneType == null) {
            this.stoneType = StoneTypes.STONE;
        }

        // the ores which the vein contain
        if (configRoot.has("ores")) {
            this.storedOres = WorldConfigUtils.createWeightedOreMap(configRoot.get("ores"), this.stoneType);
        }

        // vein name for JEI display
        if (configRoot.has("name")) {
            this.assignedName = LocalizationUtils.format(configRoot.get("name").getAsString());
        }
        // vein description for JEI display
        if (configRoot.has("description")) {
            this.description = configRoot.get("description").getAsString();
        }
        // additional weighting changes determined by biomes
        if (configRoot.has("biome_modifier")) {
            this.biomeWeightModifier = WorldConfigUtils.createBiomeWeightModifier(configRoot.get("biome_modifier"));
        }
        // filtering of dimensions to determine where the vein can generate
        if (configRoot.has("dimension_filter")) {
            this.dimensionFilter = WorldConfigUtils.createWorldPredicate(configRoot.get("dimension_filter"));
        }

        BedrockOreVeinHandler.addOreDeposit(this);

        return true;
    }
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

    public int getMinimumDensity() {
        return densities[0];
    }

    public int getMaximumDensity() {
        return densities[1];
    }

    public int getDepletionAmount() {
        return depletionAmount;
    }

    public int getDepletionChance() {
        return depletionChance;
    }

    public int getWeight() {
        return weight;
    }

    public Map<ItemStack, Integer> getStoredOres() {
        return storedOres;
    }

    public Function<Biome, Integer> getBiomeWeightModifier() {
        return biomeWeightModifier;
    }

    public Predicate<WorldProvider> getDimensionFilter() {
        return dimensionFilter;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BedrockOreDepositDefinition))
            return false;

        BedrockOreDepositDefinition objDeposit = (BedrockOreDepositDefinition) obj;
        if (this.weight != objDeposit.getWeight())
            return false;
        if ((this.assignedName == null && objDeposit.getAssignedName() != null) ||
                (this.assignedName != null && objDeposit.getAssignedName() == null) ||
                (this.assignedName != null && objDeposit.getAssignedName() != null &&
                        !this.assignedName.equals(objDeposit.getAssignedName())))
            return false;
        if ((this.description == null && objDeposit.getDescription() != null) ||
                (this.description != null && objDeposit.getDescription() == null) ||
                (this.description != null && objDeposit.getDescription() != null &&
                        !this.description.equals(objDeposit.getDescription())))
            return false;
        if ((this.biomeWeightModifier == null && objDeposit.getBiomeWeightModifier() != null) ||
                (this.biomeWeightModifier != null && objDeposit.getBiomeWeightModifier() == null) ||
                (this.biomeWeightModifier != null && objDeposit.getBiomeWeightModifier() != null &&
                        !this.biomeWeightModifier.equals(objDeposit.getBiomeWeightModifier())))
            return false;
        if ((this.dimensionFilter == null && objDeposit.getDimensionFilter() != null) ||
                (this.dimensionFilter != null && objDeposit.getDimensionFilter() == null) ||
                (this.dimensionFilter != null && objDeposit.getDimensionFilter() != null &&
                        !this.dimensionFilter.equals(objDeposit.getDimensionFilter())))
            return false;

        return super.equals(obj);
    }
}
