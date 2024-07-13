package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.MultiNetNodeHandler;
import gregtech.api.graphnet.pipenet.physical.PipeTileEntity;
import gregtech.api.graphnet.worldnet.WorldNet;
import gregtech.api.graphnet.worldnet.WorldNetNode;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.Nullable;

public final class WorldPipeNetNode extends WorldNetNode {

    @Nullable
    MultiNetNodeHandler handler;

    public WorldPipeNetNode(WorldPipeNet net) {
        super(net);
    }

    public PipeTileEntity getTileEntity() {
        // should this be cached? It's only ever used for active nodes when they are being targeted by a path traversal.
        return (PipeTileEntity) getNet().getWorld().getTileEntity(getEquivalencyData());
    }

    @Override
    public WorldPipeNet getNet() {
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
        if (handler != null) {
            return handler.traverse(this.getNet(), queryTick, simulate);
        }
        else return true;
    }

    @Override
    public BlockPos getEquivalencyData() {
        return (BlockPos) super.getEquivalencyData();
    }
}
