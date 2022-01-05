package gregtech.api.unification.ore;

import com.google.common.base.Preconditions;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTControlledRegistry;
import gregtech.common.ConfigHolder;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * For ore generation
 */
public class StoneType implements Comparable<StoneType> {

    public final String name;
    public final String harvestTool;
    public final ResourceLocation backgroundSideTexture;
    public final ResourceLocation backgroundTopTexture;

    public final OrePrefix processingPrefix;
    public final Material stoneMaterial;
    public final Supplier<IBlockState> stone;
    public final SoundType soundType;
    //we are using guava predicate because isReplaceableOreGen uses it
    @SuppressWarnings("Guava")
    private final com.google.common.base.Predicate<IBlockState> predicate;
    public final boolean shouldBeDroppedAsItem;

    public static final GTControlledRegistry<String, StoneType> STONE_TYPE_REGISTRY = new GTControlledRegistry<>(128);

    public StoneType(int id, String name, ResourceLocation backgroundSideTexture, ResourceLocation backgroundTopTexture, SoundType soundType, OrePrefix processingPrefix, Material stoneMaterial, String harvestTool, Supplier<IBlockState> stone, Predicate<IBlockState> predicate, boolean shouldBeDroppedAsItem) {
        Preconditions.checkArgument(
                stoneMaterial.hasProperty(PropertyKey.DUST),
                "Stone type must be made with a Material with the Dust Property!"
        );
        this.name = name;
        this.backgroundSideTexture = backgroundSideTexture;
        this.backgroundTopTexture = backgroundTopTexture;
        this.soundType = soundType;
        this.processingPrefix = processingPrefix;
        this.stoneMaterial = stoneMaterial;
        this.harvestTool = harvestTool;
        this.stone = stone;
        this.predicate = predicate::test;
        this.shouldBeDroppedAsItem = shouldBeDroppedAsItem || ConfigHolder.worldgen.allUniqueStoneTypes;
        STONE_TYPE_REGISTRY.register(id, name, this);
    }

    public StoneType(int id, String name, ResourceLocation backgroundTexture, SoundType soundType, OrePrefix processingPrefix, Material stoneMaterial, String harvestTool, Supplier<IBlockState> stone, Predicate<IBlockState> predicate, boolean shouldBeDroppedAsItem) {
        this(id, name, backgroundTexture, backgroundTexture, soundType, processingPrefix, stoneMaterial, harvestTool, stone, predicate, shouldBeDroppedAsItem);
    }

    @Override
    public int compareTo(@Nonnull StoneType stoneType) {
        return STONE_TYPE_REGISTRY.getIDForObject(this) - STONE_TYPE_REGISTRY.getIDForObject(stoneType);
    }

    public static void init() {
        //noinspection ResultOfMethodCallIgnored
        StoneTypes.STONE.name.getBytes();
    }

    public static StoneType computeStoneType(IBlockState blockState, IBlockAccess world, BlockPos blockPos) {
        //TODO ADD CONFIG HOOK HERE FOR MATCHING BLOCKS WITH STONE TYPES
        for (StoneType stoneType : STONE_TYPE_REGISTRY) {
            if (blockState.getBlock().isReplaceableOreGen(blockState, world, blockPos, stoneType.predicate))
                return stoneType;
        }
        return null;
    }

}
