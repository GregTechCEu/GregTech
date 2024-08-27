package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.worldnet.WorldPosNetNode;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class WorldPipeNetNode extends WorldPosNetNode {

    private static final PipeTileEntity FALLBACK = new PipeTileEntity();

    @Nullable
    MultiNodeHelper overlapHelper;

    private WeakReference<IWorldPipeNetTile> tileReference;

    public WorldPipeNetNode(WorldPipeNet net) {
        super(net);
    }

    public @NotNull IWorldPipeNetTile getTileEntity() {
        IWorldPipeNetTile tile = getTileEntity(true);
        if (tile == null) {
            // something went very wrong, return the fallback to prevent NPEs and remove us from the net.
            getNet().removeNode(this);
            tile = FALLBACK;
        }
        return tile;
    }

    @Nullable
    public IWorldPipeNetTile getTileEntityNoLoading() {
        return getTileEntity(false);
    }

    private IWorldPipeNetTile getTileEntity(boolean allowLoading) {
        if (tileReference != null) {
            IWorldPipeNetTile tile = tileReference.get();
            if (tile != null) return tile;
        }
        World world = getNet().getWorld();
        if (!allowLoading && !world.isBlockLoaded(getEquivalencyData())) return null;
        TileEntity tile = world.getTileEntity(getEquivalencyData());
        if (tile instanceof IWorldPipeNetTile pipe) {
            this.tileReference = new WeakReference<>(pipe);
            return pipe;
        } else return null;
    }

    @Override
    public void onRemove() {
        if (this.overlapHelper != null) {
            this.overlapHelper.removeNode(this);
            this.overlapHelper = null;
        }
    }

    @Override
    public @NotNull WorldPipeNet getNet() {
        return (WorldPipeNet) super.getNet();
    }

    @Override
    public WorldPipeNetNode setPos(BlockPos pos) {
        super.setPos(pos);
        this.getNet().synchronizeNode(this);
        return this;
    }

    @Override
    public boolean traverse(long queryTick, boolean simulate) {
        if (overlapHelper != null) {
            return overlapHelper.traverse(this.getNet(), queryTick, simulate);
        } else return true;
    }

    @Override
    public BlockPos getEquivalencyData() {
        return (BlockPos) super.getEquivalencyData();
    }
}
