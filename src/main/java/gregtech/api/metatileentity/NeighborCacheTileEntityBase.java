package gregtech.api.metatileentity;

import gregtech.api.metatileentity.interfaces.INeighborCache;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public abstract class NeighborCacheTileEntityBase extends SyncedTileEntityBase implements INeighborCache {

    private static final WeakReference<TileEntity> NULL = new WeakReference<>(null);
    private static final WeakReference<TileEntity> INVALID = new WeakReference<>(null);

    private final List<WeakReference<TileEntity>> neighbors = Arrays.asList(
            INVALID, INVALID, INVALID, INVALID, INVALID, INVALID);
    private boolean neighborsInvalidated = false;

    public NeighborCacheTileEntityBase() {
        invalidateNeighbors(false);
    }

    protected void invalidateNeighbors(boolean notify) {
        if (!this.neighborsInvalidated) {
            for (EnumFacing value : EnumFacing.VALUES) {
                if (notify && crossesChunk(value)) {
                    // notify neighbor on a different chunk to invalidate us
                    TileEntity neighbor = getNeighbor(value);
                    if (neighbor != null) {
                        IBlockState state = getWorld().getBlockState(neighbor.getPos());
                        state.neighborChanged(getWorld(), neighbor.getPos(), getBlockType(), getPos());
                    }
                }
                this.neighbors.set(value.getIndex(), INVALID);
            }
            this.neighborsInvalidated = true;
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void setWorld(@NotNull World worldIn) {
        super.setWorld(worldIn);
        invalidateNeighbors(false);
    }

    @MustBeInvokedByOverriders
    @Override
    public void setPos(@NotNull BlockPos posIn) {
        super.setPos(posIn);
        invalidateNeighbors(false);
    }

    @MustBeInvokedByOverriders
    @Override
    public void invalidate() {
        super.invalidate();
        invalidateNeighbors(false);
    }

    @MustBeInvokedByOverriders
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        invalidateNeighbors(true);
    }

    @Override
    public @Nullable TileEntity getNeighbor(@NotNull EnumFacing facing) {
        if (world == null || pos == null) return null;
        // if the ref is INVALID, compute neighbor, otherwise, return TE or null
        WeakReference<TileEntity> ref = invalidRef(facing) ? computeNeighbor(facing) : getRef(facing);
        return ref.get();
    }

    private boolean invalidRef(EnumFacing facing) {
        WeakReference<TileEntity> ref = getRef(facing);
        if (ref == INVALID || crossesUnloadedChunk(facing)) return true;
        TileEntity te = ref.get();
        return te != null && te.isInvalid();
    }

    private boolean crossesUnloadedChunk(EnumFacing facing) {
        if (crossesChunk(facing)) {
            int ncx = getPos().offset(facing).getX() >> 4;
            int ncz = getPos().offset(facing).getZ() >> 4;
            return getWorld().getChunkProvider().getLoadedChunk(ncx, ncz) == null;
        }
        return false;
    }

    private boolean crossesChunk(EnumFacing facing) {
        int cx = getPos().getX() >> 4, cz = getPos().getZ() >> 4;
        BlockPos offset = getPos().offset(facing);
        int ncx = offset.getX() >> 4, ncz = offset.getZ() >> 4;
        return cx != ncx || cz != ncz;
    }

    @NotNull
    private WeakReference<TileEntity> computeNeighbor(EnumFacing facing) {
        TileEntity te = super.getNeighbor(facing);
        // avoid making new references to null TEs
        WeakReference<TileEntity> ref = te == null ? NULL : new WeakReference<>(te);
        this.neighbors.set(facing.getIndex(), ref);
        this.neighborsInvalidated = false;
        return ref;
    }

    @NotNull
    private WeakReference<TileEntity> getRef(EnumFacing facing) {
        return this.neighbors.get(facing.getIndex());
    }

    public void onNeighborChanged(@NotNull EnumFacing facing) {
        this.neighbors.set(facing.getIndex(), INVALID);
    }
}
