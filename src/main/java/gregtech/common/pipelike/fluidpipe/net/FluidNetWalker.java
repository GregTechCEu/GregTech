package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.*;

public class FluidNetWalker extends PipeNetWalker {

    public static List<FluidPipeNet.Inventory> createNetData(FluidPipeNet net, World world, BlockPos sourcePipe) {
        FluidNetWalker walker = new FluidNetWalker(net, world, sourcePipe, 1, new ArrayList<>(), new HashSet<>(), Integer.MAX_VALUE, new ArrayList<>());
        TileEntity tile = world.getTileEntity(sourcePipe);
        if(tile instanceof TileEntityFluidPipe)
            walker.holdingPipes.add((TileEntityFluidPipe) tile);
        walker.traversePipeNet();
        return walker.inventories;
    }

    private final List<FluidPipeNet.Inventory> inventories;
    private final Set<Object> pathObjects;
    private final List<TileEntityFluidPipe> holdingPipes;
    private final Map<BlockPos, List<CoverFluidFilter>> covers = new HashMap<>();
    private LinkedList<PosFace> checkedCovers = new LinkedList<>();
    private int rate;

    protected FluidNetWalker(PipeNet<?> net, World world, BlockPos sourcePipe, int walkedBlocks, List<FluidPipeNet.Inventory> inventories, Set<Object> pathObjects, int rate, List<TileEntityFluidPipe> holdingPipes) {
        super(net, world, sourcePipe, walkedBlocks);
        this.inventories = inventories;
        this.pathObjects = pathObjects;
        this.rate = rate;
        this.holdingPipes = holdingPipes;
    }

    @Override
    protected PipeNetWalker createSubWalker(PipeNet<?> net, World world, BlockPos nextPos, int walkedBlocks) {
        Set<Object> pathObjectsCopy = new HashSet<>(pathObjects);
        List<CoverFluidFilter> fluidFilter = covers.get(nextPos);
        if(fluidFilter != null)
            pathObjectsCopy.addAll(fluidFilter);
        FluidNetWalker walker = new FluidNetWalker(net, world, nextPos, walkedBlocks, inventories, pathObjectsCopy, rate, new ArrayList<>(holdingPipes));
        walker.checkedCovers = checkedCovers;
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        pathObjects.addAll(covers.values());
        covers.clear();
        pathObjects.add(pipeTile);
        while (checkedCovers.size() > 12) {
            checkedCovers.removeFirst();
        }
        this.rate = Math.min(this.rate, ((TileEntityFluidPipe) pipeTile).getNodeData().throughput);
        int validPipes = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offset = pos.offset(facing);
            EnumFacing opposite = facing.getOpposite();
            TileEntity tile = pipeTile.getPipeWorld().getTileEntity(offset);
            if (tile instanceof TileEntityFluidPipe) {
                if(!checkedCovers.contains(new PosFace(pos, facing))) {
                    CoverBehavior cover = pipeTile.getCoverableImplementation().getCoverAtSide(facing);
                    if(cover instanceof CoverFluidFilter)
                        putFilter(offset, cover);
                }
                ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, opposite);
                PosFace posFace = new PosFace(offset, opposite);
                if(!checkedCovers.contains(posFace))
                    checkedCovers.addLast(posFace);
                if(coverable != null) {
                    CoverBehavior cover2 = coverable.getCoverAtSide(opposite);
                    if(cover2 instanceof CoverFluidFilter)
                        putFilter(offset, cover2);
                }
                validPipes++;
            }
        }
        if (validPipes > 2) {
            holdingPipes.add((TileEntityFluidPipe) pipeTile);
        }
    }

    private void putFilter(BlockPos pos, CoverBehavior cover) {
        List<CoverFluidFilter> coverFluidFilters = covers.get(pos);
        if(coverFluidFilters == null)
            coverFluidFilters = new ArrayList<>();
        coverFluidFilters.add((CoverFluidFilter) cover);
        covers.put(pos, coverFluidFilters);
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null) return;
        IFluidHandler handler = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, faceToNeighbour.getOpposite());
        if (handler != null) {
            Set<Object> pathObjectsCopy = new HashSet<>(pathObjects);
            List<CoverFluidFilter> fluidFilter = covers.get(pipePos.offset(faceToNeighbour));
            if(fluidFilter != null)
                pathObjectsCopy.addAll(fluidFilter);
            List<TileEntityFluidPipe> holders = new ArrayList<>(holdingPipes);
            holders.add((TileEntityFluidPipe) pipeTile);
            inventories.add(new FluidPipeNet.Inventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), pathObjectsCopy, rate, holders));
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return neighbourPipe instanceof TileEntityFluidPipe;
    }

    private static class PosFace {
        private final BlockPos pos;
        private final EnumFacing face;

        private PosFace(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PosFace posFace = (PosFace) o;
            return Objects.equals(pos, posFace.pos) && face == posFace.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, face);
        }
    }
}
