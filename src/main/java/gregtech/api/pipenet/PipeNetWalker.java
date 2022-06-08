package gregtech.api.pipenet;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTLog;
import gregtech.common.pipelike.itempipe.net.ItemNetWalker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This is a helper class to get information about a pipe net
 * <p>The walker is written that it will always find the shortest path to any destination
 * <p>On the way it can collect information about the pipes and it's neighbours
 * <p>After creating a walker simply call {@link #traversePipeNet()} to start walking, then you can just collect the data
 * <p><b>Do not walk a walker more than once</b>
 * <p>For example implementations look at {@link ItemNetWalker}
 */
public abstract class PipeNetWalker {

    private PipeNetWalker root;
    private final World world;
    private final Set<Long> walked = new HashSet<>();
    private final List<EnumFacing> pipes = new ArrayList<>();
    private List<PipeNetWalker> walkers;
    private final BlockPos.MutableBlockPos currentPos;
    private int walkedBlocks;
    private boolean invalid;
    private boolean running;
    private boolean failed = false;

    protected PipeNetWalker(World world, BlockPos sourcePipe, int walkedBlocks) {
        this.world = Objects.requireNonNull(world);
        this.walkedBlocks = walkedBlocks;
        this.currentPos = new BlockPos.MutableBlockPos(Objects.requireNonNull(sourcePipe));
        this.root = this;
    }

    /**
     * Creates a sub walker
     * Will be called when a pipe has multiple valid pipes
     *
     * @param world        world
     * @param nextPos      next pos to check
     * @param walkedBlocks distance from source in blocks
     * @return new sub walker
     */
    protected abstract PipeNetWalker createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos, int walkedBlocks);

    /**
     * You can increase walking stats here. for example
     *
     * @param pipeTile current checking pipe
     * @param pos      current pipe pos
     */
    protected abstract void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos);

    /**
     * Checks the neighbour of the current pos
     *
     * @param pipePos         current pos
     * @param faceToNeighbour face to neighbour
     * @param neighbourTile   neighbour tile
     */
    protected abstract void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile);

    /**
     * If the pipe is valid to perform a walk on
     *
     * @param currentPipe     current pipe
     * @param neighbourPipe   neighbour pipe to check
     * @param pipePos         current pos (tile.getPipePos() != pipePos)
     * @param faceToNeighbour face to pipeTile
     * @return if the pipe is valid
     */
    protected abstract boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour);

    /**
     * Called when a sub walker is done walking
     *
     * @param subWalker the finished sub walker
     */
    protected void onRemoveSubWalker(PipeNetWalker subWalker) {
    }

    public void traversePipeNet() {
        traversePipeNet(32768);
    }

    /**
     * Starts walking the pipe net and gathers information.
     *
     * @param maxWalks max walks to prevent possible stack overflow
     * @throws IllegalStateException if the walker already walked
     */
    public void traversePipeNet(int maxWalks) {
        if (invalid)
            throw new IllegalStateException("This walker already walked. Create a new one if you want to walk again");
        int i = 0;
        running = true;
        while (running && !walk() && i++ < maxWalks) ;
        running = false;
        root.walked.clear();
        if (i >= maxWalks)
            GTLog.logger.fatal("The walker reached the maximum amount of walks {}", i);
        invalid = true;
    }

    private boolean walk() {
        if (walkers == null) {
            checkPos();

            if (pipes.size() == 0)
                return true;
            if (pipes.size() == 1) {
                currentPos.move(pipes.get(0));
                walkedBlocks++;
                return !isRunning();
            }

            walkers = new ArrayList<>();
            for (EnumFacing side : pipes) {
                PipeNetWalker walker = Objects.requireNonNull(createSubWalker(world, side, currentPos.offset(side), walkedBlocks + 1), "Walker can't be null");
                walker.root = root;
                walkers.add(walker);
            }
        }
        Iterator<PipeNetWalker> iterator = walkers.iterator();
        while (iterator.hasNext()) {
            PipeNetWalker walker = iterator.next();
            if (walker.walk()) {
                onRemoveSubWalker(walker);
                iterator.remove();
            }
        }

        return !isRunning() || walkers.size() == 0;
    }

    private void checkPos() {
        pipes.clear();
        TileEntity thisPipe = world.getTileEntity(currentPos);
        IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) thisPipe;
        if (pipeTile == null) {
            if (walkedBlocks == 1) {
                // if it is the first block, it wasn't already checked
                GTLog.logger.error("First PipeTile is null during walk at {}", currentPos);
                this.failed = true;
                return;
            } else
                throw new IllegalStateException("PipeTile was not null last walk, but now is");
        }
        checkPipe(pipeTile, currentPos);
        root.walked.add(pipeTile.getPipePos().toLong());

        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        // check for surrounding pipes and item handlers
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            //skip sides reported as blocked by pipe network
            if (!pipeTile.isConnected(accessSide))
                continue;

            pos.setPos(currentPos).move(accessSide);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IPipeTile) {
                IPipeTile<?, ?> otherPipe = (IPipeTile<?, ?>) tile;
                if (!otherPipe.isConnected(accessSide.getOpposite()) || otherPipe.isFaceBlocked(accessSide.getOpposite()) || isWalked(otherPipe))
                    continue;
                if (isValidPipe(pipeTile, otherPipe, currentPos, accessSide)) {
                    pipes.add(accessSide);
                    continue;
                }
            }
            checkNeighbour(pipeTile, currentPos, accessSide, tile);
        }
        pos.release();
    }

    protected boolean isWalked(IPipeTile<?, ?> pipe) {
        return root.walked.contains(pipe.getPipePos().toLong());
    }

    /**
     * Will cause the root walker to stop after the next walk
     */
    public void stop() {
        root.running = false;
    }

    public boolean isRunning() {
        return root.running;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getCurrentPos() {
        return currentPos;
    }

    public int getWalkedBlocks() {
        return walkedBlocks;
    }

    public boolean isRoot() {
        return this.root == this;
    }

    public boolean isFailed() {
        return failed;
    }
}
