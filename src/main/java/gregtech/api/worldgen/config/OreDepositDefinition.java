package gregtech.api.worldgen.config;

import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.WorldBlockPredicate;
import gregtech.api.worldgen.filler.BlockFiller;
import gregtech.api.worldgen.populator.IVeinPopulator;
import gregtech.api.worldgen.shape.ShapeGenerator;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public class OreDepositDefinition implements IWorldgenDefinition {

    public static final Function<Biome, Integer> NO_BIOME_INFLUENCE = biome -> 0;
    public static final Predicate<WorldProvider> PREDICATE_SURFACE_WORLD = WorldProvider::isSurfaceWorld;
    public static final WorldBlockPredicate PREDICATE_STONE_TYPE = (state, world, pos) -> StoneType
            .computeStoneType(state, world, pos) != null;

    private final String depositName;

    private int weight;
    private int priority;
    private float density;
    private String assignedName;
    private String description;
    private final int[] heightLimit = new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE };
    private boolean countAsVein = true;

    private Function<Biome, Integer> biomeWeightModifier = NO_BIOME_INFLUENCE;
    private Predicate<WorldProvider> dimensionFilter = PREDICATE_SURFACE_WORLD;
    private WorldBlockPredicate generationPredicate = PREDICATE_STONE_TYPE;
    private IVeinPopulator veinPopulator;

    private BlockFiller blockFiller;
    private ShapeGenerator shapeGenerator;

    public OreDepositDefinition(String depositName) {
        this.depositName = depositName;
    }

    @Override
    public boolean initializeFromConfig(@NotNull JsonObject configRoot) {
        this.weight = configRoot.get("weight").getAsInt();
        this.density = configRoot.get("density").getAsFloat();
        if (configRoot.has("name")) {
            this.assignedName = LocalizationUtils.format(configRoot.get("name").getAsString());
        }
        if (configRoot.has("description")) {
            this.description = configRoot.get("description").getAsString();
        }
        if (configRoot.has("priority")) {
            this.priority = configRoot.get("priority").getAsInt();
        }
        if (configRoot.has("count_as_vein")) {
            this.countAsVein = configRoot.get("count_as_vein").getAsBoolean();
        }
        if (configRoot.has("min_height")) {
            this.heightLimit[0] = configRoot.get("min_height").getAsInt();
        }
        if (configRoot.has("max_height")) {
            this.heightLimit[1] = configRoot.get("max_height").getAsInt();
        }
        if (configRoot.has("biome_modifier")) {
            this.biomeWeightModifier = WorldConfigUtils.createBiomeWeightModifier(configRoot.get("biome_modifier"));
        }
        if (configRoot.has("dimension_filter")) {
            this.dimensionFilter = WorldConfigUtils.createWorldPredicate(configRoot.get("dimension_filter"));
        }
        if (configRoot.has("generation_predicate")) {
            this.generationPredicate = PredicateConfigUtils
                    .createBlockStatePredicate(configRoot.get("generation_predicate"));
        }
        if (configRoot.has("vein_populator")) {
            JsonObject object = configRoot.get("vein_populator").getAsJsonObject();
            this.veinPopulator = WorldGenRegistry.INSTANCE.createVeinPopulator(object);
        }
        this.blockFiller = WorldGenRegistry.INSTANCE.createBlockFiller(configRoot.get("filler").getAsJsonObject());
        this.shapeGenerator = WorldGenRegistry.INSTANCE
                .createShapeGenerator(configRoot.get("generator").getAsJsonObject());

        if (veinPopulator != null) {
            veinPopulator.initializeForVein(this);
        }
        return true;
    }

    /**
     * Must be converted using {@link gregtech.api.util.FileUtility#slashToNativeSep(String)}
     * before it can be used as a file path
     */
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

    public float getDensity() {
        return density;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isVein() {
        return countAsVein;
    }

    public boolean checkInHeightLimit(int yLevel) {
        return yLevel >= heightLimit[0] && yLevel <= heightLimit[1];
    }

    public int[] getHeightLimit() {
        return heightLimit;
    }

    public int getMinimumHeight() {
        return heightLimit[0];
    }

    public int getMaximumHeight() {
        return heightLimit[1];
    }

    public Function<Biome, Integer> getBiomeWeightModifier() {
        return biomeWeightModifier;
    }

    public Predicate<WorldProvider> getDimensionFilter() {
        return dimensionFilter;
    }

    public WorldBlockPredicate getGenerationPredicate() {
        return generationPredicate;
    }

    public IVeinPopulator getVeinPopulator() {
        return veinPopulator;
    }

    public BlockFiller getBlockFiller() {
        return blockFiller;
    }

    public ShapeGenerator getShapeGenerator() {
        return shapeGenerator;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OreDepositDefinition))
            return false;

        OreDepositDefinition objDeposit = (OreDepositDefinition) obj;
        if (this.weight != objDeposit.getWeight())
            return false;
        if (this.density != objDeposit.getDensity())
            return false;
        if (this.priority != objDeposit.getPriority())
            return false;
        if (this.countAsVein != objDeposit.isVein())
            return false;
        if (this.getMinimumHeight() != objDeposit.getMinimumHeight())
            return false;
        if (this.getMaximumHeight() != objDeposit.getMaximumHeight())
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
        if ((this.generationPredicate == null && objDeposit.getGenerationPredicate() != null) ||
                (this.generationPredicate != null && objDeposit.getGenerationPredicate() == null) ||
                (this.generationPredicate != null && objDeposit.getGenerationPredicate() != null &&
                        !this.generationPredicate.equals(objDeposit.getGenerationPredicate())))
            return false;
        if ((this.veinPopulator == null && objDeposit.getVeinPopulator() != null) ||
                (this.veinPopulator != null && objDeposit.getVeinPopulator() == null) ||
                (this.veinPopulator != null && objDeposit.getVeinPopulator() != null &&
                        !this.veinPopulator.equals(objDeposit.getVeinPopulator())))
            return false;

        return super.equals(obj);
    }
}
