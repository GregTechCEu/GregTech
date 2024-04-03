package gregtech.common.pipelike;

import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipeCollectorWalker<T extends IPipeTile<?, ?>> extends PipeNetWalker<T> {

    @NotNull
    @SuppressWarnings("unchecked")
    public static List<IPipeTile<?, ?>> collectPipeNet(World world, BlockPos sourcePipe, IPipeTile<?, ?> pipe,
                                                       int limit) {
        PipeCollectorWalker<? extends IPipeTile<?, ?>> walker = (PipeCollectorWalker<? extends IPipeTile<?, ?>>) new PipeCollectorWalker<>(
                world, sourcePipe, 0, pipe.getClass());
        walker.traversePipeNet(limit);
        return walker.isFailed() ? Collections.EMPTY_LIST : Collections.unmodifiableList(walker.pipes);
    }

    public static List<IPipeTile<?, ?>> collectPipeNet(World world, BlockPos sourcePipe, IPipeTile<?, ?> pipe) {
        // Default limit for a pipenet walker
        return collectPipeNet(world, sourcePipe, pipe, 32768);
    }

    // I love type erasure
    private final Class<T> basePipeClass;
    private List<T> pipes = new ArrayList<>();
    private BlockPos sourcePipe;

    protected PipeCollectorWalker(World world, BlockPos sourcePipe, int walkedBlocks, Class<T> basePipeClass) {
        super(world, sourcePipe, walkedBlocks);
        this.sourcePipe = sourcePipe;
        this.basePipeClass = basePipeClass;
    }

    @Override
    protected PipeNetWalker<T> createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos,
                                               int walkedBlocks) {
        PipeCollectorWalker<T> walker = new PipeCollectorWalker<>(world, nextPos, walkedBlocks, this.basePipeClass);
        walker.pipes = pipes;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected void checkPipe(T pipeTile, BlockPos pos) {
        this.pipes.add(pipeTile);
    }

    @Override
    protected void checkNeighbour(T pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour,
                                  @Nullable TileEntity neighbourTile) {}

    @Override
    protected Class<T> getBasePipeClass() {
        return basePipeClass;
    }
}
