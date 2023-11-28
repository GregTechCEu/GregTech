package gregtech.api.unification.ore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTControlledRegistry;
import gregtech.common.ConfigHolder;
import gregtech.integration.jei.basic.OreByProduct;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * For ore generation
 */
public class StoneType implements Comparable<StoneType> {

    public final String name;

    public final OrePrefix processingPrefix;
    public final Material stoneMaterial;
    public final Supplier<IBlockState> stone;
    public final SoundType soundType;
    // we are using guava predicate because isReplaceableOreGen uses it
    @SuppressWarnings("Guava")
    private final com.google.common.base.Predicate<IBlockState> predicate;
    public final boolean shouldBeDroppedAsItem;

    public static final GTControlledRegistry<String, StoneType> STONE_TYPE_REGISTRY = new GTControlledRegistry<>(128);

    public StoneType(int id, String name, SoundType soundType, OrePrefix processingPrefix, Material stoneMaterial,
                     Supplier<IBlockState> stone, Predicate<IBlockState> predicate, boolean shouldBeDroppedAsItem) {
        Preconditions.checkArgument(
                stoneMaterial.hasProperty(PropertyKey.DUST),
                "Stone type must be made with a Material with the Dust Property!");
        this.name = name;
        this.soundType = soundType;
        this.processingPrefix = processingPrefix;
        this.stoneMaterial = stoneMaterial;
        this.stone = stone;
        this.predicate = predicate::test;
        this.shouldBeDroppedAsItem = shouldBeDroppedAsItem || ConfigHolder.worldgen.allUniqueStoneTypes;
        STONE_TYPE_REGISTRY.register(id, name, this);
        if (Loader.isModLoaded(GTValues.MODID_JEI) && this.shouldBeDroppedAsItem) {
            OreByProduct.addOreByProductPrefix(this.processingPrefix);
        }
    }

    @Override
    public int compareTo(@NotNull StoneType stoneType) {
        return STONE_TYPE_REGISTRY.getIDForObject(this) - STONE_TYPE_REGISTRY.getIDForObject(stoneType);
    }

    private static final ThreadLocal<Boolean> hasDummyPredicateRan = ThreadLocal.withInitial(() -> false);
    private static final com.google.common.base.Predicate<IBlockState> dummyPredicate = state -> {
        hasDummyPredicateRan.set(true);
        return false;
    };

    public static void init() {
        // noinspection ResultOfMethodCallIgnored
        StoneTypes.STONE.name.getBytes();
    }

    public static StoneType computeStoneType(IBlockState state, IBlockAccess world, BlockPos pos) {
        // First: check if this Block's isReplaceableOreGen even considers the predicate passed through
        boolean dummy$isReplaceableOreGen = state.getBlock().isReplaceableOreGen(state, world, pos, dummyPredicate);
        if (hasDummyPredicateRan.get()) {
            // Current Block's isReplaceableOreGen does indeed consider the predicate
            // Reset hasDummyPredicateRan for the next test
            hasDummyPredicateRan.set(false);
            // Pass through actual predicates and test for real
            for (StoneType stoneType : STONE_TYPE_REGISTRY) {
                if (state.getBlock().isReplaceableOreGen(state, world, pos, stoneType.predicate)) {
                    // Found suitable match
                    return stoneType;
                }
            }
        } else if (dummy$isReplaceableOreGen) {
            // It is not considered, but the test still returned true (this means the impl was probably very lazily
            // done)
            // We have to test against the IBlockState ourselves to see if there's a suitable StoneType
            for (StoneType stoneType : STONE_TYPE_REGISTRY) {
                if (stoneType.predicate.test(state)) {
                    // Found suitable match
                    return stoneType;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoneType stoneType = (StoneType) o;

        if (shouldBeDroppedAsItem != stoneType.shouldBeDroppedAsItem) return false;
        if (!name.equals(stoneType.name)) return false;
        if (!processingPrefix.equals(stoneType.processingPrefix)) return false;
        if (!stoneMaterial.equals(stoneType.stoneMaterial)) return false;
        if (!stone.equals(stoneType.stone)) return false;
        if (!soundType.equals(stoneType.soundType)) return false;
        return predicate.equals(stoneType.predicate);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + processingPrefix.hashCode();
        result = 31 * result + stoneMaterial.hashCode();
        result = 31 * result + stone.hashCode();
        result = 31 * result + soundType.hashCode();
        result = 31 * result + predicate.hashCode();
        result = 31 * result + (shouldBeDroppedAsItem ? 1 : 0);
        return result;
    }
}
