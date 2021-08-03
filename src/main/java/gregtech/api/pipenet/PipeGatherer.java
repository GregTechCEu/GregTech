package gregtech.api.pipenet;

import gregtech.api.pipenet.tile.IPipeTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PipeGatherer extends PipeNetWalker {

    public static List<IPipeTile<?, ?>> gatherPipes(PipeNet<?> net, World world, BlockPos sourcePipe, Predicate<IPipeTile<?, ?>> pipePredicate) {
        PipeGatherer gatherer = new PipeGatherer(net, world, sourcePipe, 1, pipePredicate, new ArrayList<>());
        gatherer.traversePipeNet();
        return gatherer.pipes;
    }

    public static List<IPipeTile<?, ?>> gatherPipesInDistance(PipeNet<?> net, World world, BlockPos sourcePipe, Predicate<IPipeTile<?, ?>> pipePredicate, int distance) {
        PipeGatherer gatherer = new PipeGatherer(net, world, sourcePipe, 1, pipePredicate, new ArrayList<>());
        gatherer.traversePipeNet(distance);
        return gatherer.pipes;
    }

    private final Predicate<IPipeTile<?, ?>> pipePredicate;
    private final List<IPipeTile<?, ?>> pipes;

    protected PipeGatherer(PipeNet<?> net, World world, BlockPos sourcePipe, int walkedBlocks, Predicate<IPipeTile<?, ?>> pipePredicate, List<IPipeTile<?, ?>> pipes) {
        super(net, world, sourcePipe, walkedBlocks);
        this.pipePredicate = pipePredicate;
        this.pipes = pipes;
    }

    @Override
    protected PipeNetWalker createSubWalker(PipeNet<?> net, World world, BlockPos nextPos, int walkedBlocks) {
        return new PipeGatherer(net, world, nextPos, walkedBlocks, pipePredicate, pipes);
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        if(pipePredicate.test(pipeTile)) {
            pipes.add(pipeTile);
        }
    }

    @Override
    protected void checkNeighbour(BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return pipePredicate.test(neighbourPipe);
    }
}
