package gregtech.api.metatileentity.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface defining access to cached neighboring tile entities to a block or tile entity
 */
public interface INeighborCache extends IHasWorldObjectAndCoords {

    /**
     * @param facing the side at which the neighbor is located
     * @return the neighboring tile entity at the side
     */
    default @Nullable TileEntity getNeighbor(@NotNull EnumFacing facing) {
        return world().getTileEntity(pos().offset(facing));
    }

    default @Nullable TileEntity getNeighborNoChunkloading(@NotNull EnumFacing facing) {
        BlockPos pos = pos().offset(facing);
        return world().isBlockLoaded(pos) ? world().getTileEntity(pos) : null;
    }

    /**
     * Called when an adjacent neighboring block has changed at a side in some way
     *
     * @param facing the side at which the neighbor has changed
     */
    void onNeighborChanged(@NotNull EnumFacing facing);
}
