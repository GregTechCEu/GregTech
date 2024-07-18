package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.PatternMatchContext;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
     * @return A context for the formed structure, or null if the structure check failed.
     */
    PatternMatchContext checkPatternFastAt(World world, BlockPos centerPos, EnumFacing frontFacing,
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
     * Gets the default shape, if the multiblock does not specify one.
     */
    PreviewBlockPattern getDefaultShape();

    /**
     * Clears the cache for checkPatternFastAt(...) in case something in the pattern is changed.
     */
    void clearCache();

    /**
     * If anything from legacy need to be updated.
     * 
     * @return True if yes, which will throw an error in the multiblock.
     */
    default boolean legacyBuilderError() {
        return false;
    }
}
