package gregtech.common.pipelike;

import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PipeCollectorWalker<T extends IPipeTile<?, ?>> extends PipeNetWalker<T> {

    public static void collectPipeNet(@NotNull World world, @NotNull BlockPos sourcePipe, @NotNull IPipeTile<?, ?> pipe,
                                      @NotNull Predicate<IPipeTile<?, ?>> pipeFunction) {
        PipeCollectorWalker<? extends IPipeTile<?, ?>> walker = (PipeCollectorWalker<? extends IPipeTile<?, ?>>) new PipeCollectorWalker<>(
                world, sourcePipe, 0, pipe.getClass(), pipeFunction);
        walker.traversePipeNet();
    }

    // I love type erasure - htmlcsjs
    private final Class<T> basePipeClass;

    /**
     * Function to run on every pipe
     * If false is returned then halt the walker
     */
    @NotNull
    private final Predicate<IPipeTile<?, ?>> pipeFunction;

    private BlockPos sourcePipe;

    protected PipeCollectorWalker(@NotNull World world, @NotNull BlockPos sourcePipe, int walkedBlocks,
                                  @NotNull Class<T> basePipeClass, @NotNull Predicate<IPipeTile<?, ?>> pipeFunction) {
        super(world, sourcePipe, walkedBlocks);
        this.sourcePipe = sourcePipe;
        this.basePipeClass = basePipeClass;
        this.pipeFunction = pipeFunction;
    }

    @Override
    protected PipeNetWalker<T> createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos,
                                               int walkedBlocks) {
        PipeCollectorWalker<T> walker = new PipeCollectorWalker<>(world, nextPos, walkedBlocks, this.basePipeClass,
                pipeFunction);
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected void checkPipe(T pipeTile, BlockPos pos) {
        if (!this.pipeFunction.test(pipeTile)) {
            this.root.stop();
        }
    }

    @Override
    protected void checkNeighbour(T pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour,
                                  @Nullable TileEntity neighbourTile) {}

    @Override
    protected Class<T> getBasePipeClass() {
        return basePipeClass;
    }
}
