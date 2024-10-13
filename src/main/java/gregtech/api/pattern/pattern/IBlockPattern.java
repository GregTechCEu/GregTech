package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.OriginOffset;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param map Pass in an empty map to receive a populated map with the chars in the return mapping to a simpel predicate.
     * @param directions Pass in an array of length 3 to get the directions for the preview, or null if you don't want it.
     */
    // peak nullability, basically the return value can be null but if it is not then no arrays can be null
    char @Nullable [] @NotNull [] @NotNull [] getDefaultShape(Char2ObjectMap<TraceabilityPredicate.SimplePredicate> map, @Nullable RelativeDirection[] directions);

    /**
     * Autobuilds a pattern, using the player inventory(and maybe AE soontm) items if in survival.
     * @param player The player initiating this autobuild.
     * @param map The map from the multiblock builder.
     */
    void autoBuild(EntityPlayer player, Map<String, String> map);


    /**
     * Gets the internal pattern state, you should use the one returned from
     * {@link IBlockPattern#checkPatternFastAt(World, BlockPos, EnumFacing, EnumFacing, boolean)} always
     * except for the shouldUpdate field.
     */
    PatternState getPatternState();

    /**
     * Clears the cache for checkPatternFastAt(...) in case something in the pattern is changed. Default impl just getCache and then clears it.
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
