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

    public static List<FluidPipeNet.Inventory> createNetData(World world, BlockPos sourcePipe) {
        FluidNetWalker walker = new FluidNetWalker(world, sourcePipe, 1, new ArrayList<>(), new HashSet<>(), Integer.MAX_VALUE, new HashSet<>());
        walker.traversePipeNet();
        return walker.inventories;
    }

    private final List<FluidPipeNet.Inventory> inventories;
    private final Set<FluidNode> nodes;
    private final Set<CoverFluidFilter> filters;
    private final Map<BlockPos, List<CoverFluidFilter>> covers = new HashMap<>();
    private LinkedList<PosFace> checkedCovers = new LinkedList<>();
    private int rate;

    protected FluidNetWalker(World world, BlockPos sourcePipe, int walkedBlocks, List<FluidPipeNet.Inventory> inventories, Set<FluidNode> nodes, int rate, Set<CoverFluidFilter> filters) {
        super(world, sourcePipe, walkedBlocks);
        this.inventories = inventories;
        this.nodes = nodes;
        this.rate = rate;
        this.filters = filters;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        Set<FluidNode> nodes = new HashSet<>(this.nodes);
        Collection<CoverFluidFilter> fluidFilter = covers.get(nextPos);
        if(fluidFilter != null)
            filters.addAll(fluidFilter);
        FluidNetWalker walker = new FluidNetWalker(world, nextPos, walkedBlocks, inventories, nodes, rate, new HashSet<>(filters));
        walker.checkedCovers = checkedCovers;
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        for(List<CoverFluidFilter> filters1 : covers.values()) {
            filters.addAll(filters1);
        }
        covers.clear();
        if(pipeTile.getNode() instanceof FluidNode) {
            FluidNode node = (FluidNode) pipeTile.getNode();
            if(nodes.add(node)) {
                this.rate = Math.min(this.rate, node.getNodeData().throughput);
            }
        }
        while (checkedCovers.size() > 12) {
            checkedCovers.removeFirst();
        }
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offset = pos.offset(facing);
            EnumFacing opposite = facing.getOpposite();
            TileEntity tile = pipeTile.getWorld().getTileEntity(offset);
            if (tile instanceof TileEntityFluidPipe) {
                if(!checkedCovers.contains(new PosFace(pos, facing))) {
                    CoverBehavior cover = pipeTile.getCoverable().getCoverAtSide(facing);
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
            }
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
            Set<FluidNode> nodes = new HashSet<>(this.nodes);
            Collection<CoverFluidFilter> fluidFilter = covers.get(pipePos.offset(faceToNeighbour));
            if(fluidFilter != null)
                filters.addAll(fluidFilter);
            inventories.add(new FluidPipeNet.Inventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), nodes, rate, new HashSet<>(filters)));
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
