package gregtech.api.pattern.pattern;

import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Matrix4f;

import java.util.Map;

public interface IBlockPattern {

    // a[i] is RelativeDirection.values()[i].getRelativeFacing(NORTH, UP)
    EnumFacing[] DEFAULT_FACINGS = {
            RelativeDirection.VALUES[0].getRelativeFacing(EnumFacing.NORTH, EnumFacing.UP),
            RelativeDirection.VALUES[1].getRelativeFacing(EnumFacing.NORTH, EnumFacing.UP),
            RelativeDirection.VALUES[2].getRelativeFacing(EnumFacing.NORTH, EnumFacing.UP),
            RelativeDirection.VALUES[3].getRelativeFacing(EnumFacing.NORTH, EnumFacing.UP),
            RelativeDirection.VALUES[4].getRelativeFacing(EnumFacing.NORTH, EnumFacing.UP),
            RelativeDirection.VALUES[5].getRelativeFacing(EnumFacing.NORTH, EnumFacing.UP)
    };

    /**
     * @return The internal state of the pattern if it is valid, or null;
     */
    @Nullable
    PatternState cachedPattern(World world);

    /**
     * Checks the whole pattern, you should probably use checkPatternFastAt(...) instead.
     * 
     * @param world     The world the multiblock is in.
     * @param transform Matrix transforming coordinates(specified by the specific impl) into world coordinates.
     * @return True if the check passed, in which case the context is mutated for returning from checkPatternFastAt(...)
     */
    boolean checkPatternAt(World world, Matrix4f transform);

    /**
     * Gets the default shape, if the multiblock does not specify one. Return null to represent the default shape does
     * not exist.
     *
     * @param transform Matrix transforming coordinates(specified by the specific impl) into world coordinates.
     * @param keyMap    The map from multiblock builder for autobuild.
     * @return The long key is using {@link gregtech.api.pattern.GreggyBlockPos#toLong(int, int, int)} with x, y, z
     *         respectively being. The map is sorted using the natural ordering(thus with x, y, z order).
     */
    Long2ObjectSortedMap<TraceabilityPredicate> getDefaultShape(Matrix4f transform,
                                                                @NotNull Map<String, String> keyMap);

    /**
     * Gets the internal pattern state, probably use the one returned from
     * {@link IBlockPattern#cachedPattern(World)}
     */
    PatternState getPatternState();

    /**
     * Clears the cache for checkPatternFastAt(...) in case something in the pattern is changed. Default impl just
     * getCache and then clears it.
     */
    default void clearCache() {
        getCache().clear();
    }

    /**
     * Gets the cache, do not modify. Note that the cache stores everything in the AABB of the substructure, except for
     * any() TraceabilityPredicates.
     * 
     * @return The cache for rapid pattern checking.
     */
    Long2ObjectMap<BlockInfo> getCache();

    void moveOffset(RelativeDirection dir, int amount);

    default void moveOffset(RelativeDirection dir) {
        moveOffset(dir, 1);
    }
}
