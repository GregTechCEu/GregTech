package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.covers.*;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FluidNetHandler implements IFluidHandler {

    private final FluidPipeNet net;
    private final TileEntityFluidPipe pipe;
    private final EnumFacing facing;
    private int simulatedTransfers = 0;

    public FluidNetHandler(FluidPipeNet net, TileEntityFluidPipe pipe, EnumFacing facing) {
        this.net = net;
        this.pipe = Objects.requireNonNull(pipe);
        this.facing = facing;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        FluidStack[] netFluids = pipe.getContainedFluids();
        FluidTankProperties[] properties = new FluidTankProperties[netFluids.length];
        for (int i = 0; i < netFluids.length; i++) {
            properties[i] = new FluidTankProperties(netFluids[i], Integer.MAX_VALUE, true, false);
        }
        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || resource.getFluid() == null) return 0;
        if ((pipe.findChannel(resource)) < 0) return 0;
        simulatedTransfers = 0;
        CoverBehavior pipeCover = getCoverOnPipe(pipe.getPipePos(), facing);
        CoverBehavior tileCover = getCoverOnNeighbour(pipe.getPipePos(), facing);

        boolean pipePump = pipeCover instanceof CoverPump, tilePump = tileCover instanceof CoverPump;
        // abort if there are two conveyors
        if (pipePump && tilePump) return 0;

        if (tileCover != null && !checkImportCover(tileCover, false, resource))
            return 0;

        if (!pipePump && !tilePump)
            return insertFirst(resource, doFill);

        CoverPump pump = (CoverPump) (pipePump ? pipeCover : tileCover);
        if (pump.getPumpMode() == (pipePump ? CoverPump.PumpMode.IMPORT : CoverPump.PumpMode.EXPORT) &&
                pump.getDistributionMode() == DistributionMode.ROUND_ROBIN) {
            return insertRoundRobin(resource, doFill);
        }

        return insertFirst(resource, doFill);
    }

    public boolean checkImportCover(CoverBehavior cover, boolean onPipe, FluidStack stack) {
        if (cover instanceof CoverFluidFilter) {
            CoverFluidFilter filter = (CoverFluidFilter) cover;
            return (filter.getFilterMode() != FluidFilterMode.FILTER_BOTH &&
                    (filter.getFilterMode() != FluidFilterMode.FILTER_FILL || !onPipe) &&
                    (filter.getFilterMode() != FluidFilterMode.FILTER_DRAIN || onPipe)) || filter.testFluidStack(stack);
        }
        return true;
    }

    protected List<Handler> createHandlers() {
        List<Handler> handlers = new ArrayList<>();
        for (FluidPipeNet.Inventory inv : net.getNetData(pipe.getPipePos())) {
            if (pipe.getPipePos().equals(inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
                continue;
            IFluidHandler handler = inv.getHandler(pipe.getPipeWorld());
            if (handler != null)
                handlers.add(new Handler(handler, inv));
        }
        return handlers;
    }

    protected int insertFirst(FluidStack stack, boolean doFill) {
        int amount = stack.amount;
        for (Handler handler : createHandlers()) {
            stack.amount -= insert(handler, stack, doFill);
            if (stack.amount == 0)
                return amount;
        }
        return amount - stack.amount;
    }

    protected int insertRoundRobin(FluidStack stack, boolean doFill) {
        List<Handler> handlers = createHandlers();
        int amount = stack.amount;

        if (handlers.size() == 0)
            return 0;
        if (handlers.size() == 1)
            return insert(handlers.get(0), stack, doFill);
        stack.amount -= insertToHandlers(handlers, stack, doFill);
        if (stack.amount != 0 && handlers.size() > 0)
            stack.amount -= insertToHandlers(handlers, stack, doFill);
        return amount - stack.amount;
    }

    public int insertToHandlers(List<Handler> handlers, FluidStack stack, boolean doFill) {
        Iterator<Handler> handlerIterator = handlers.iterator();
        int inserted = 0;
        int count = stack.amount;
        int c = count / handlers.size();
        int m = count % handlers.size();
        while (handlerIterator.hasNext()) {
            int amount = c;
            if (m > 0) {
                amount++;
                m--;
            }
            if (amount == 0) break;
            FluidStack toInsert = stack.copy();
            toInsert.amount = amount;
            Handler handler = handlerIterator.next();
            int i = insert(handler, toInsert, doFill);
            if (i > 0)
                inserted += i;
            if (i < amount)
                handlerIterator.remove();
        }
        return inserted;
    }

    private int insert(Handler handler, FluidStack stack, boolean doFill) {
        int allowed = checkTransferable(pipe, handler.getMinThroughput(), stack.amount, doFill);
        if (allowed == 0) return 0;
        CoverBehavior pipeCover = getCoverOnPipe(handler.getPipePos(), handler.getFaceToHandler());
        CoverBehavior tileCover = getCoverOnNeighbour(handler.getPipePos(), handler.getFaceToHandler());
        if (pipeCover instanceof CoverFluidRegulator && tileCover instanceof CoverFluidRegulator)
            return 0;
        if (pipeCover != null && !checkExportCover(pipeCover, true, stack))
            return 0;

        if (pipeCover instanceof CoverFluidRegulator && ((CoverFluidRegulator) pipeCover).getPumpMode() == CoverPump.PumpMode.EXPORT)
            return insertOverRegulator(handler, (CoverFluidRegulator) pipeCover, stack, doFill, allowed);
        if (tileCover instanceof CoverFluidRegulator && ((CoverFluidRegulator) tileCover).getPumpMode() == CoverPump.PumpMode.IMPORT)
            return insertOverRegulator(handler, (CoverFluidRegulator) tileCover, stack, doFill, allowed);

        return insert(handler, stack, doFill, allowed);
    }

    private int insert(Handler handler, FluidStack stack, boolean doFill, int max) {
        if(stack == null || stack.amount <= 0 || max <= 0) return 0;

        for (TileEntityFluidPipe tickingPipe : handler.getHoldingPipes())
            if (!tickingPipe.findAndSetChannel(stack))
                return 0;

        IFluidHandler fluidHandler = handler.handler;
        // check every pipe in path, but only if the last transferred fluid is not the same as the current
        if (!stack.isFluidEqual(handler.getLastTransferredFluid())) {
            boolean isGaseous = stack.getFluid().isGaseous(stack);
            int temp = stack.getFluid().getTemperature(stack);
            for (Object o : handler.getObjectsInPath()) {
                if (o instanceof TileEntityFluidPipe) {
                    TileEntityFluidPipe pipe = (TileEntityFluidPipe) o;
                    FluidPipeProperties properties = pipe.getNodeData();
                    boolean isLeakingPipe = isGaseous && !properties.gasProof;
                    boolean isBurningPipe = temp > properties.maxFluidTemperature;
                    if (isLeakingPipe || isBurningPipe) {
                        net.destroyNetwork(pipe.getPos(), isLeakingPipe, isBurningPipe, temp);
                        return 0;
                    }
                } else if (o instanceof CoverFluidFilter) {
                    if (!((CoverFluidFilter) o).testFluidStack(stack))
                        return 0;
                }
            }
            handler.setLastTransferredFluid(stack);
        }

        if (max >= stack.amount) {
            int inserted = fluidHandler.fill(stack, doFill);
            if (inserted > 0) {
                stack.amount = inserted;
                if (doFill)
                    for (TileEntityFluidPipe tickingPipe : handler.getHoldingPipes())
                        tickingPipe.setContainingFluid(stack, tickingPipe.getCurrentChannel());
                transfer(pipe, doFill, inserted);
            }
            return inserted;
        }
        FluidStack toInsert = stack.copy();
        toInsert.amount = Math.min(max, stack.amount);
        int inserted = fluidHandler.fill(toInsert, doFill);
        if (inserted > 0) {
            toInsert.amount = inserted;
            if (doFill)
                for (TileEntityFluidPipe tickingPipe : handler.getHoldingPipes())
                    tickingPipe.setContainingFluid(toInsert, tickingPipe.getCurrentChannel());
            transfer(pipe, doFill, inserted);
        }
        return inserted;
    }

    public int insertOverRegulator(Handler handler, CoverFluidRegulator regulator, FluidStack stack, boolean doFill, int allowed) {
        int rate = regulator.getTransferAmount();
        int count;
        switch (regulator.getTransferMode()) {
            case TRANSFER_ANY:
                return insert(handler, stack, doFill, allowed);
            case KEEP_EXACT:
                count = rate - countStack(handler.handler, stack, regulator);
                if (count <= 0) return 0;
                count = Math.min(allowed, Math.min(stack.amount, count));
                return insert(handler, stack, doFill, count);
            case TRANSFER_EXACT:
                //int max = allowed + regulator.getBuffer();
                count = Math.min(allowed, Math.min(rate, stack.amount));
                if (count < rate) {
                    return 0;
                }
                if (insert(handler, stack, false, count) != rate) {
                    return 0;
                }
                return insert(handler, stack, doFill, count);
        }
        return 0;
    }

    public int countStack(IFluidHandler handler, FluidStack stack, CoverFluidRegulator arm) {
        if (arm == null) return 0;
        int count = 0;
        for (IFluidTankProperties property : handler.getTankProperties()) {
            FluidStack tank = property.getContents();
            if (tank == null) continue;
            if (tank.isFluidEqual(stack)) {
                count += tank.amount;
            }
        }
        return count;
    }

    public boolean checkExportCover(CoverBehavior cover, boolean onPipe, FluidStack stack) {
        if (cover instanceof CoverFluidFilter) {
            CoverFluidFilter filter = (CoverFluidFilter) cover;
            return (filter.getFilterMode() != FluidFilterMode.FILTER_BOTH &&
                    (filter.getFilterMode() != FluidFilterMode.FILTER_FILL || onPipe) &&
                    (filter.getFilterMode() != FluidFilterMode.FILTER_DRAIN || !onPipe)) || filter.testFluidStack(stack);
        }
        return true;
    }

    public CoverBehavior getCoverOnPipe(BlockPos pos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pos);
        if (tile instanceof TileEntityFluidPipe) {
            ICoverable coverable = ((TileEntityFluidPipe) tile).getCoverableImplementation();
            return coverable.getCoverAtSide(handlerFacing);
        }
        return null;
    }

    public CoverBehavior getCoverOnNeighbour(BlockPos pos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pos.offset(handlerFacing));
        if (tile != null) {
            ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, handlerFacing.getOpposite());
            if (coverable == null) return null;
            return coverable.getCoverAtSide(handlerFacing.getOpposite());
        }
        return null;
    }

    private int checkTransferable(TileEntityFluidPipe pipe, int throughput, int amount, boolean doFill) {
        if (doFill)
            return Math.max(0, Math.min(throughput - pipe.getTransferredFluids(), amount));
        else
            return Math.max(0, Math.min(throughput - (pipe.getTransferredFluids() + simulatedTransfers), amount));
    }

    private void transfer(TileEntityFluidPipe pipe, boolean doFill, int amount) {
        if (doFill) {
            pipe.transferFluid(amount);
        } else
            simulatedTransfers += amount;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    private static class Handler extends FluidPipeNet.Inventory {
        private final IFluidHandler handler;

        public Handler(IFluidHandler handler, FluidPipeNet.Inventory inv) {
            super(inv.getPipePos(), inv.getFaceToHandler(), inv.getDistance(), inv.getObjectsInPath(), inv.getMinThroughput(), inv.getHoldingPipes());
            setLastTransferredFluid(inv.getLastTransferredFluid());
            this.handler = handler;
        }
    }
}
