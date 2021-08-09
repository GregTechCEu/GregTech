package gregtech.api.worldgen.config;

import com.google.gson.JsonObject;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBiome;
import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.WorldBlockPredicate;
import gregtech.api.worldgen.filler.BlockFiller;
import gregtech.api.worldgen.populator.IVeinPopulator;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;
import gregtech.api.worldgen.shape.ShapeGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Optional.Method;
import org.apache.commons.lang3.ArrayUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.function.Function;
import java.util.function.Predicate;

@ZenClass("mods.gregtech.ore.OreDepositDefinition")
@ZenRegister
public class OreDepositDefinition {

    public static final Function<Biome, Integer> NO_BIOME_INFLUENCE = biome -> 0;
    public static final Predicate<WorldProvider> PREDICATE_SURFACE_WORLD = WorldProvider::isSurfaceWorld;
    public static final WorldBlockPredicate PREDICATE_STONE_TYPE = (state, world, pos) -> StoneType.computeStoneType(state, world, pos) != null;

    private final String depositName;

    private int weight;
    private int priority;
    private float density;
    private String assignedName;
    private String description;
    private final int[] heightLimit = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
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

    public void initializeFromConfig(JsonObject configRoot) {
        this.weight = configRoot.get("weight").getAsInt();
        this.density = configRoot.get("density").getAsFloat();
        if (configRoot.has("name")) {
            this.assignedName = configRoot.get("name").getAsString();
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
            this.generationPredicate = PredicateConfigUtils.createBlockStatePredicate(configRoot.get("generation_predicate"));
        }
        //legacy surface rock specifier support
        if (configRoot.has("surface_stone_material")) {
            Material surfaceStoneMaterial = OreConfigUtils.getMaterialByName(configRoot.get("surface_stone_material").getAsString());
            if (!surfaceStoneMaterial.hasProperty(PropertyKey.ORE)) {
                throw new IllegalArgumentException("Material " + surfaceStoneMaterial + " doesn't have surface rock variant");
            }
            this.veinPopulator = new SurfaceRockPopulator(surfaceStoneMaterial);
        }
        if (configRoot.has("vein_populator")) {
            JsonObject object = configRoot.get("vein_populator").getAsJsonObject();
            this.veinPopulator = WorldGenRegistry.INSTANCE.createVeinPopulator(object);
        }
        this.blockFiller = WorldGenRegistry.INSTANCE.createBlockFiller(configRoot.get("filler").getAsJsonObject());
        this.shapeGenerator = WorldGenRegistry.INSTANCE.createShapeGenerator(configRoot.get("generator").getAsJsonObject());

        if (veinPopulator != null) {
            veinPopulator.initializeForVein(this);
        }
    }

    //This is the file name
    @ZenGetter("depositName")
    public String getDepositName() {
        return depositName;
    }

    @ZenGetter("assignedName")
    public String getAssignedName() {
        return assignedName;
    }

    @ZenGetter("description")
    public String getDescription() {
        return description;
    }

    @ZenGetter("weight")
    public int getWeight() {
        return weight;
    }

    @ZenGetter("density")
    public float getDensity() {
        return density;
    }

    @ZenGetter("priority")
    public int getPriority() {
        return priority;
    }

    @ZenGetter("isVein")
    public boolean isVein() {
        return countAsVein;
    }

    @ZenMethod
    public boolean checkInHeightLimit(int yLevel) {
        return yLevel >= heightLimit[0] && yLevel <= heightLimit[1];
    }

    @ZenMethod
    public int[] getHeightLimit() {
        return heightLimit;
    }

    @ZenGetter("minimumHeight")
    public int getMinimumHeight() {
        return heightLimit[0];
    }

    @ZenGetter("maximumHeight")
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

    @ZenMethod("getBiomeWeightModifier")
    @Method(modid = GTValues.MODID_CT)
    public int ctGetBiomeWeightModifier(IBiome biome) {
        int biomeIndex = ArrayUtils.indexOf(CraftTweakerMC.biomes, biome);
        Biome mcBiome = Biome.REGISTRY.getObjectById(biomeIndex);
        return mcBiome == null ? 0 : getBiomeWeightModifier().apply(mcBiome);
    }

    @ZenMethod("checkDimension")
    @Method(modid = GTValues.MODID_CT)
    public boolean ctCheckDimension(int dimensionId) {
        WorldProvider worldProvider = DimensionManager.getProvider(dimensionId);
        return worldProvider != null && getDimensionFilter().test(worldProvider);
    }

    @ZenMethod("canGenerateIn")
    @Method(modid = GTValues.MODID_CT)
    public boolean ctCanGenerateIn(crafttweaker.api.block.IBlockState blockState, crafttweaker.api.world.IBlockAccess blockAccess, crafttweaker.api.world.IBlockPos blockPos) {
        IBlockState mcBlockState = CraftTweakerMC.getBlockState(blockState);
        return getGenerationPredicate().test(mcBlockState, (IBlockAccess) blockAccess.getInternal(), (BlockPos) blockPos.getInternal());
    }

    @ZenGetter("filter")
    public BlockFiller getBlockFiller() {
        return blockFiller;
    }

    @ZenGetter("shape")
    public ShapeGenerator getShapeGenerator() {
        return shapeGenerator;
    }
}
