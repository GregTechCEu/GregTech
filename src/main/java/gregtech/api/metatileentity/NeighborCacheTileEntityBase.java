package gregtech.api.metatileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class NeighborCacheTileEntityBase extends SyncedTileEntityBase {

    private final TileEntity[] neighbors = new TileEntity[6];
    private boolean neighborsInvalidated = false;

    public NeighborCacheTileEntityBase() {
        invalidateNeighbors();
    }

    protected void invalidateNeighbors() {
        if (!this.neighborsInvalidated) {
            Arrays.fill(this.neighbors, this);
            this.neighborsInvalidated = true;
        }
    }

    @Override
    public void setWorld(@NotNull World worldIn) {
        super.setWorld(worldIn);
        invalidateNeighbors();
    }

    @Override
    public void setPos(@NotNull BlockPos posIn) {
        super.setPos(posIn);
        invalidateNeighbors();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateNeighbors();
    }

    @Override
    public TileEntity getNeighbor(EnumFacing facing) {
        if (world == null || pos == null) return null;
        int i = facing.getIndex();
        TileEntity neighbor = this.neighbors[i];
        if (neighbor == this) {
            neighbor = world.getTileEntity(pos.offset(facing));
            this.neighbors[i] = neighbor;
            this.neighborsInvalidated = false;
        }
        return neighbor;
    }

    public void onNeighborChanged(EnumFacing facing) {
        this.neighbors[facing.getIndex()] = this;
    }
}
