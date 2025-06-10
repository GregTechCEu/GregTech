package gregtech.api.metatileentity;

import gregtech.api.metatileentity.interfaces.INeighborCache;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public abstract class NeighborCacheTileEntityBase extends SyncedTileEntityBase implements INeighborCache {

    private static final WeakReference<TileEntity> NULL = new WeakReference<>(null);
    private static final WeakReference<TileEntity> INVALID = new WeakReference<>(null);

    @SuppressWarnings("rawtypes")
    private final WeakReference[] neighbors = new WeakReference[6];
    private boolean neighborsInvalidated = false;

    public NeighborCacheTileEntityBase() {
        invalidateNeighbors();
    }

    protected void invalidateNeighbors() {
        if (!this.neighborsInvalidated) {
            Arrays.fill(this.neighbors, INVALID);
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
        if (world == null || pos == null) return null;
        WeakReference<TileEntity> ref = invalidRef(facing) ? computeNeighbor(facing) : getRef(facing);
        return ref.get();
    }

    // if true, compute neighbor, if false, return TE or null
    private boolean invalidRef(EnumFacing facing) {
        WeakReference<TileEntity> ref = getRef(facing);
        if (ref == INVALID) return true;
        TileEntity te = ref.get();
        return te != null && te.isInvalid();
    }

    private WeakReference<TileEntity> computeNeighbor(EnumFacing facing) {
        TileEntity te = super.getNeighbor(facing);
        // avoid making new references to null TEs
        this.neighbors[facing.ordinal()] = te == null ? NULL : new WeakReference<>(te);
        this.neighborsInvalidated = false;
        return getRef(facing);
    }

    @SuppressWarnings("unchecked")
    private WeakReference<TileEntity> getRef(EnumFacing facing) {
        return (WeakReference<TileEntity>) this.neighbors[facing.ordinal()];
    }

    public void onNeighborChanged(@NotNull EnumFacing facing) {
        this.neighbors[facing.ordinal()] = INVALID;
    }
}
