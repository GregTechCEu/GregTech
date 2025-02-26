package gregtech.api.metatileentity;

import gregtech.api.metatileentity.interfaces.INeighborCache;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class NeighborCacheTileEntityBase extends SyncedTileEntityBase implements INeighborCache {

    private final TileEntity[] neighbors = new TileEntity[6];
    private boolean neighborsInvalidated = false;

    /**
     * @param doInvalidationHere set to false if you override {@link NeighborCacheTileEntityBase#invalidateNeighbors()}
     *                           with a method that references something you do not yet have set.
     */
    public NeighborCacheTileEntityBase(boolean doInvalidationHere) {
        if (doInvalidationHere) invalidateNeighbors();
    }

    public NeighborCacheTileEntityBase() {
        invalidateNeighbors();
    }

    protected void invalidateNeighbors() {
        if (!this.neighborsInvalidated) {
            Arrays.fill(this.neighbors, this);
            this.neighborsInvalidated = true;
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void setWorld(@NotNull World worldIn) {
        super.setWorld(worldIn);
        invalidateNeighbors();
    }

    @MustBeInvokedByOverriders
    @Override
    public void setPos(@NotNull BlockPos posIn) {
        super.setPos(posIn);
        invalidateNeighbors();
    }

    @MustBeInvokedByOverriders
    @Override
    public void invalidate() {
        super.invalidate();
        invalidateNeighbors();
    }

    @MustBeInvokedByOverriders
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        invalidateNeighbors();
    }

    @Override
    public @Nullable TileEntity getNeighbor(@NotNull EnumFacing facing) {
        if (world() == null || pos() == null) return null;
        int i = facing.getIndex();
        TileEntity neighbor = this.neighbors[i];
        if (neighbor == this || (neighbor != null && neighbor.isInvalid())) {
            neighbor = world().getTileEntity(pos().offset(facing));
            this.neighbors[i] = neighbor;
            this.neighborsInvalidated = false;
        }
        return neighbor;
    }

    @Override
    public @Nullable TileEntity getNeighborNoChunkloading(@NotNull EnumFacing facing) {
        if (world() == null || pos() == null) return null;
        int i = facing.getIndex();
        TileEntity neighbor = this.neighbors[i];
        if (neighbor == this || (neighbor != null && neighbor.isInvalid())) {
            BlockPos pos = pos().offset(facing);
            if (world().isBlockLoaded(pos)) {
                neighbor = world().getTileEntity(pos);
                this.neighbors[i] = neighbor;
                this.neighborsInvalidated = false;
            } else return null;
        }
        return neighbor;
    }

    public void onNeighborChanged(@NotNull EnumFacing facing) {
        this.neighbors[facing.getIndex()] = this;
    }
}
