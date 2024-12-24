package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.OriginOffset;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IBlockPattern {

    /**
     * Checks the pattern fast, this should always be preferred to checkPatternAt(...) for multiblock code.
     * 
     * @param world         The world the multiblock is in.
     * @param centerPos     The position of the controller.
     * @param frontFacing   The front facing of the controller, obtained via
     *                      {@link MultiblockControllerBase#getFrontFacing()}
     * @param upwardsFacing The up facing of the controller, obtained via
     *                      {@link MultiblockControllerBase#getUpwardsFacing()}
     * @param allowsFlip    Whether the multiblock allows flipping.
     * @return The internal state of the pattern. Check whether its valid first before using other fields.
     */
    @NotNull
    PatternState checkPatternFastAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                    EnumFacing upwardsFacing, boolean allowsFlip);

    /**
     * Checks the whole pattern, you should probably use checkPatternFastAt(...) instead.
     * 
     * @param world         The world the multiblock is in.
     * @param centerPos     The position of the controller.
     * @param frontFacing   The front facing of the controller, obtained via
     *                      {@link MultiblockControllerBase#getFrontFacing()}
     * @param upwardsFacing The up facing of the controller, obtained via
     *                      {@link MultiblockControllerBase#getUpwardsFacing()}
     * @param isFlipped     Is the multiblock flipped or not.
     * @return True if the check passed, in which case the context is mutated for returning from checkPatternFastAt(...)
     */
    boolean checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing, EnumFacing upwardsFacing,
                           boolean isFlipped);

    /**
     * Gets the default shape, if the multiblock does not specify one. Return null to represent the default shape does
     * not exist.
     *
     * @param keyMap The map from multiblock builder for autobuild.
     * @return The long key is using {@link gregtech.api.pattern.GreggyBlockPos#toLong(int, int, int)} with x, y, z
     *         respectively being. The map is sorted using the natural ordering(thus with x, y, z order).
     */
    Long2ObjectSortedMap<TraceabilityPredicate> getDefaultShape(MultiblockControllerBase src,
                                                                @NotNull Map<String, String> keyMap);

    /**
     * Gets the internal pattern state, you should use the one returned from
     * {@link IBlockPattern#checkPatternFastAt(World, BlockPos, EnumFacing, EnumFacing, boolean)} always
     * except for the shouldUpdate field.
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

    OriginOffset getOffset();

    default void moveOffset(RelativeDirection dir, int amount) {
        getOffset().move(dir, amount);
    }

    default void moveOffset(RelativeDirection dir) {
        getOffset().move(dir);
    }
}
