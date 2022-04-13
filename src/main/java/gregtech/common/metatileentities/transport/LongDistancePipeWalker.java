package gregtech.common.metatileentities.transport;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.api.util.GTLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public abstract class LongDistancePipeWalker {

    private LongDistancePipeWalker root;
    private final World world;
    private final Set<Long> walked = new HashSet<>();
    private final List<EnumFacing> pipes = new ArrayList<>();
    private final BlockPos.MutableBlockPos currentPos;
    private final BlockPos startPos;
    private int walkedBlocks;
    private boolean invalid;
    private boolean running;
    private MetaTileEntityPipelineEndpoint endpoint;
    private int distance = 0;

    private final List<LongDistancePipeWalker> subWalkers = new ArrayList<>();
    private final List<LongDistancePipeWalker> subWalkerQueue = new ArrayList<>();
    private boolean splitUp = false;

    protected LongDistancePipeWalker(World world, BlockPos sourcePipe, int walkedBlocks) {
        this.world = Objects.requireNonNull(world);
        this.walkedBlocks = walkedBlocks;
        this.currentPos = new BlockPos.MutableBlockPos(Objects.requireNonNull(sourcePipe));
        this.startPos = sourcePipe;
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
    protected abstract LongDistancePipeWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks);

    protected abstract boolean isValidEndpoint(MetaTileEntityPipelineEndpoint endpoint);

    /**
     * If the pipe is valid to perform a walk on
     *
     * @param blockState      state of the pipe block
     * @param pipePos         current pos (tile.getPipePos() != pipePos)
     * @param faceToNeighbour face to pipeTile
     * @return if the pipe is valid
     */
    protected abstract boolean isValidPipe(IBlockState blockState, BlockPos pipePos, EnumFacing faceToNeighbour);

    /**
     * Called when a sub walker is done walking
     *
     * @param subWalker the finished sub walker
     */
    protected void onRemoveSubWalker(LongDistancePipeWalker subWalker) {
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

        MetaTileEntityPipelineEndpoint.startTimer();
        MetaTileEntity mte = MetaTileEntity.tryGet(world, currentPos);
        if (mte instanceof MetaTileEntityPipelineEndpoint && isValidEndpoint((MetaTileEntityPipelineEndpoint) mte)) {
            root.walked.add(currentPos.toLong());
            currentPos.move(mte.getFrontFacing().getOpposite());
        }

        int i = 0;
        running = true;
        while (running && !walk() && i++ < maxWalks) ;
        running = false;
        MetaTileEntityPipelineEndpoint.endTimer(root.walked.size());
        root.walked.clear();
        if (i >= maxWalks)
            GTLog.logger.fatal("The walker reached the maximum amount of walks {}", i);
        invalid = true;
    }

    private boolean walk() {
        if (!this.splitUp) {
            checkPos();

            if (pipes.size() == 0)
                return true;
            if (pipes.size() == 1) {
                currentPos.move(pipes.get(0));
                walkedBlocks++;
                return !isRunning();
            }

            this.splitUp = true;
            //walkers = new ArrayList<>();
            for (EnumFacing side : pipes) {
                LongDistancePipeWalker walker = Objects.requireNonNull(createSubWalker(world, currentPos.offset(side), walkedBlocks + 1), "Walker can't be null");
                walker.root = root;
                //walkers.add(walker);
                root.subWalkerQueue.add(walker);
            }
        }
        if (isRoot()) {
            if (!root.subWalkerQueue.isEmpty()) {
                root.subWalkers.addAll(root.subWalkerQueue);
                root.subWalkerQueue.clear();
            }
            root.subWalkers.removeIf(walker -> {
                walker.walk();
                return walker.splitUp;
            });
            return !isRunning() || root.subWalkers.size() == 0;
        }
        /*Iterator<LongDistancePipeWalker> iterator = walkers.iterator();
        while (iterator.hasNext()) {
            LongDistancePipeWalker walker = iterator.next();
            if (walker.walk()) {
                onRemoveSubWalker(walker);
                iterator.remove();
            }
        }*/

        return !isRunning() || splitUp;
    }

    private void checkPos() {
        pipes.clear();
        root.walked.add(currentPos.toLong());

        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        // check for surrounding pipes and item handlers
        for (EnumFacing accessSide : EnumFacing.VALUES) {
            pos.setPos(currentPos).move(accessSide);
            if (isWalked(pos)) {
                continue;
            }
            IBlockState blockState = world.getBlockState(pos);
            if (isValidPipe(blockState, pos, accessSide)) {
                pipes.add(accessSide);
                continue;
            }
            MetaTileEntity mte = MetaTileEntity.tryGet(world, pos);
            if (mte instanceof MetaTileEntityPipelineEndpoint && isValidEndpoint((MetaTileEntityPipelineEndpoint) mte) && root.endpoint == null) {
                root.endpoint = (MetaTileEntityPipelineEndpoint) mte;
                root.distance = walkedBlocks;
                stop();
            }
        }
        pos.release();
    }

    protected boolean isWalked(BlockPos pos) {
        return root.walked.contains(pos.toLong());
    }

    /**
     * Will cause the root walker to stop after the next walk
     */
    public void stop() {
        running = false;
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

    public MetaTileEntityPipelineEndpoint getEndpoint() {
        return endpoint;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public int getDistance() {
        return distance;
    }

    public void reset() {
        if (isRoot() && !isRunning()) {
            this.currentPos.setPos(startPos);
            this.walked.clear();
            this.endpoint = null;
            this.pipes.clear();
            this.running = false;
            this.walkedBlocks = 0;
            this.invalid = false;
            this.distance = 0;
        }
    }
}
