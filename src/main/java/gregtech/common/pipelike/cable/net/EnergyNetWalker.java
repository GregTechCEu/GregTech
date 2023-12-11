package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnergyNetWalker extends PipeNetWalker<TileEntityCable> {

    public static List<EnergyRoutePath> createNetData(World world, BlockPos sourcePipe) {
        if (!(world.getTileEntity(sourcePipe) instanceof TileEntityCable)) {
            return null;
        }
        EnergyNetWalker walker = new EnergyNetWalker(world, sourcePipe, 1, new ArrayList<>());
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.routes;
    }

    private final List<EnergyRoutePath> routes;
    private TileEntityCable[] pipes = {};
    private int loss;

    protected EnergyNetWalker(World world, BlockPos sourcePipe, int walkedBlocks, List<EnergyRoutePath> routes) {
        super(world, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @Override
    protected PipeNetWalker<TileEntityCable> createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos,
                                                             int walkedBlocks) {
        EnergyNetWalker walker = new EnergyNetWalker(world, nextPos, walkedBlocks, routes);
        walker.loss = loss;
        walker.pipes = pipes;
        return walker;
    }

    @Override
    protected void checkPipe(TileEntityCable pipeTile, BlockPos pos) {
        pipes = ArrayUtils.add(pipes, pipeTile);
        loss += pipeTile.getNodeData().getLossPerBlock();
    }

    @Override
    protected void checkNeighbour(TileEntityCable pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour,
                                  @Nullable TileEntity neighbourTile) {
        // assert that the last added pipe is the current pipe
        if (pipeTile != pipes[pipes.length - 1]) throw new IllegalStateException(
                "The current pipe is not the last added pipe. Something went seriously wrong!");
        if (neighbourTile != null) {
            IEnergyContainer container = neighbourTile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                    faceToNeighbour.getOpposite());
            if (container != null) {
                routes.add(new EnergyRoutePath(faceToNeighbour, pipes, getWalkedBlocks(), loss));
            }
        }
    }

    @Override
    protected Class<TileEntityCable> getBasePipeClass() {
        return TileEntityCable.class;
    }
}
