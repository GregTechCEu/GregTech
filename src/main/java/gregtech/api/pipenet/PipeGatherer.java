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

    @Nullable
    public static IPipeTile<?, ?> findFirstMatching(World world, BlockPos sourcePipe, Predicate<IPipeTile<?, ?>> pipePredicate) {
        PipeGatherer gatherer = new PipeGatherer(world, sourcePipe, 1, pipePredicate, new ArrayList<>());
        gatherer.returnAfterFirst = true;
        gatherer.traversePipeNet();
        return gatherer.pipes.size() > 0 ? gatherer.pipes.get(0) : null;
    }

    public static List<IPipeTile<?, ?>> gatherPipes(World world, BlockPos sourcePipe, Predicate<IPipeTile<?, ?>> pipePredicate) {
        PipeGatherer gatherer = new PipeGatherer(world, sourcePipe, 1, pipePredicate, new ArrayList<>());
        gatherer.traversePipeNet();
        return gatherer.pipes;
    }

    public static List<IPipeTile<?, ?>> gatherPipesInDistance(World world, BlockPos sourcePipe, Predicate<IPipeTile<?, ?>> pipePredicate, int distance) {
        PipeGatherer gatherer = new PipeGatherer(world, sourcePipe, 1, pipePredicate, new ArrayList<>());
        gatherer.traversePipeNet(distance);
        return gatherer.pipes;
    }

    private final Predicate<IPipeTile<?, ?>> pipePredicate;
    private final List<IPipeTile<?, ?>> pipes;
    private boolean returnAfterFirst = false;

    protected PipeGatherer(World world, BlockPos sourcePipe, int walkedBlocks, Predicate<IPipeTile<?, ?>> pipePredicate, List<IPipeTile<?, ?>> pipes) {
        super(world, sourcePipe, walkedBlocks);
        this.pipePredicate = pipePredicate;
        this.pipes = pipes;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        return new PipeGatherer(world, nextPos, walkedBlocks, pipePredicate, pipes);
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        if (pipePredicate.test(pipeTile)) {
            pipes.add(pipeTile);
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return (!returnAfterFirst || pipes.size() <= 0) && pipePredicate.test(neighbourPipe);
    }
}
