package gregtech.api.pattern.pattern;

import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
            EnumFacing.UP,
            EnumFacing.DOWN,
            EnumFacing.WEST,
            EnumFacing.EAST,
            EnumFacing.NORTH,
            EnumFacing.SOUTH
    };

    /**
     * @return The internal state of the pattern if it is valid, or null;
     */
    @Nullable
    PatternState cachedPattern(World world);

    /**
     * @return True if the check passed, in which case the context is mutated for returning from checkPatternFastAt(...)
     */
    boolean checkPatternAt(World world, Matrix4f transform);

    /**
     * Gets the default shape, if the multiblock does not specify one. Return null to represent the default shape does
     * not exist.
     *
     * @param transform Matrix transforming coordinates(specified by the specific impl) into world coordinates.
     * @param keyMap    The map from multiblock builder for autobuild.
     * @return Expected that each invocation returns a new map instance.
     */
    Long2ObjectSortedMap<TraceabilityPredicate> getDefaultShape(Matrix4f transform,
                                                                @NotNull Map<String, String> keyMap);

    PatternState getState();

    default void clearCache() {
        getCache().clear();
    }

    Long2ObjectMap<BlockInfo> getCache();

    void moveOffset(RelativeDirection dir, int amount);

    default void moveOffset(RelativeDirection dir) {
        moveOffset(dir, 1);
    }

    /**
     * Common code for validating a structure's cache.
     * @return The state iff cache is valid.
     */
    default PatternState validateCache(World world) {
        if (!getCache().isEmpty()) {
            boolean pass = true;
            GreggyBlockPos gregPos = new GreggyBlockPos();
            for (Long2ObjectMap.Entry<BlockInfo> entry : getCache().long2ObjectEntrySet()) {
                BlockPos pos = gregPos.fromLong(entry.getLongKey()).immutable();
                IBlockState blockState = world.getBlockState(pos);

                if (blockState != entry.getValue().getBlockState()) {
                    pass = false;
                    break;
                }

                TileEntity cachedTileEntity = entry.getValue().getTileEntity();

                if (cachedTileEntity != null) {
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity != cachedTileEntity) {
                        pass = false;
                        break;
                    }
                }
            }
            if (pass) {
                if (getState().hasError()) {
                    getState().setState(PatternState.EnumCheckState.INVALID);
                } else {
                    getState().setState(PatternState.EnumCheckState.VALID_CACHED);
                }

                return getState();
            }
        }
        return null;
    }
}
