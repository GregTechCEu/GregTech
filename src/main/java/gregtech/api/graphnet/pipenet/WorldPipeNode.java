package gregtech.api.graphnet.pipenet;

import gregtech.api.GTValues;
import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.GraphClassType;
import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.net.BlockPosNode;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class WorldPipeNode extends BlockPosNode
                           implements NodeWithFacingToOthers, NodeWithCovers, NodeExposingCapabilities {

    public static final GraphClassType<WorldPipeNode> TYPE = new GraphClassType<>(GTValues.MODID, "WorldPipeNode",
            WorldPipeNode::resolve);

    private static final PipeTileEntity FALLBACK = new PipeTileEntity();

    @Nullable
    MultiNodeHelper overlapHelper;

    private WeakReference<IWorldPipeNetTile> tileReference;

    public WorldPipeNode(WorldPipeNet net) {
        super(net);
    }

    private static WorldPipeNode resolve(IGraphNet net) {
        if (net instanceof WorldPipeNet w) return new WorldPipeNode(w);
        GTLog.logger.fatal(
                "Attempted to initialize a WorldPipeNode to a non-WorldPipeNet. If relevant NPEs occur later, this is most likely the cause.");
        return null;
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
    public WorldPipeNode setPos(BlockPos pos) {
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
    public @NotNull BlockPos getEquivalencyData() {
        return super.getEquivalencyData();
    }

    @Override
    public @NotNull GraphClassType<? extends WorldPipeNode> getType() {
        return TYPE;
    }

    @Override
    public @Nullable EnumFacing getFacingToOther(@NotNull NetNode other) {
        return other instanceof WorldPipeNode n ?
                GTUtility.getFacingToNeighbor(this.getEquivalencyData(), n.getEquivalencyData()) : null;
    }

    @Override
    public @Nullable CoverableView getCoverableView() {
        return getTileEntity().getCoverHolder();
    }

    @Override
    public @NotNull ICapabilityProvider getProvider() {
        return getTileEntity();
    }
}
