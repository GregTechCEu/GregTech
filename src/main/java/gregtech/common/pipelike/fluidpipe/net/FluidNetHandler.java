package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.DistributionMode;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FluidNetHandler implements IFluidHandler, IFluidTank {

    private static final Logger log = GTLog.logger;

    private FluidStack stack;
    private final FluidPipeNet net;
    private TileEntityFluidPipeTickable pipe;
    private EnumFacing facing;
    private int simulatedTransfers = 0;
    private int capacity = 0;

    public FluidNetHandler(FluidPipeNet net) {
        this.stack = null;
        this.net = net;
    }

    public FluidNetHandler with(TileEntityFluidPipe pipe, EnumFacing facing) {
        if (pipe instanceof TileEntityFluidPipeTickable)
            this.pipe = (TileEntityFluidPipeTickable) pipe;
        else
            this.pipe = (TileEntityFluidPipeTickable) pipe.setSupportsTicking();
        this.facing = facing;
        this.capacity = net.getNodeData().throughput;
        return this;
    }

    public void emptyTank() {
        this.stack = null;
    }

    protected void setContainingFluid(FluidStack stack) {
        this.stack = stack;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{
                new FluidTankProperties(stack, getCapacity(), true, false) {
                    @Override
                    public boolean canFillFluidType(FluidStack fluidStack) {
                        return stack == null || stack.isFluidEqual(fluidStack);
                    }
                }
        };
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return stack;
    }

    @Override
    public int getFluidAmount() {
        return stack.amount;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(stack, getCapacity());
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || resource.getFluid() == null) return 0;
        if (stack != null && !resource.isFluidEqual(stack)) return 0;
        log.info("Try insert {}", resource);
        simulatedTransfers = 0;
        Tuple<CoverPump, Boolean> tuple = getCoverAtPipe(pipe.getPos(), facing);
        if (exportsToPipe(tuple)) {
            if (tuple.getFirst().getDistributionMode() == DistributionMode.ROUND_ROBIN) {
                return insertRoundRobin(stack, doFill);
            }
        }
        return insertFirst(resource, doFill);
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
            stack.amount = insert(handler, stack, doFill);
            if (stack.amount == 0)
                return amount;
        }
        return stack.amount;
    }

    protected int insertRoundRobin(FluidStack stack, boolean doFill) {
        List<Handler> handlers = createHandlers();
        int amount = stack.amount;

        if (handlers.size() == 0)
            return 0;
        if (handlers.size() == 1)
            return insert(handlers.get(0), stack, doFill);
        stack.amount = insertToHandlers(handlers, stack, doFill);
        if (stack.amount != amount && handlers.size() > 0)
            stack.amount = insertToHandlers(handlers, stack, doFill);
        return stack.amount;
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
        int allowed = checkTransferable(pipe, pipe.getNodeData().throughput, stack.amount, doFill);
        if (allowed == 0) return 0;
        Tuple<CoverPump, Boolean> tuple = getCoverAtPipe(handler.getPipePos(), handler.getFaceToHandler());
        if (tuple != null) {
            if (!tuple.getFirst().getFluidFilterContainer().testFluidStack(stack))
                return 0;
            boolean exportsFromPipe = exportsToPipe(tuple);
            if (exportsFromPipe && tuple.getFirst().blocksInput())
                return 0;
            if (tuple.getFirst() instanceof CoverFluidRegulator && !exportsFromPipe)
                return insertOverRegulator(handler.handler, (CoverFluidRegulator) tuple.getFirst(), tuple.getSecond(), stack, doFill, allowed);
        }
        return insert(handler.handler, stack, doFill, allowed);
    }

    private int insert(IFluidHandler handler, FluidStack stack, boolean doFill, int max) {
        if (max >= stack.amount) {
            int inserted = handler.fill(stack, doFill);
            if (inserted > 0)
                transfer(pipe, doFill, inserted);
            return inserted;
        }
        FluidStack toInsert = stack.copy();
        toInsert.amount = Math.min(max, stack.amount);
        int inserted = handler.fill(toInsert, doFill);
        if (inserted > 0)
            transfer(pipe, doFill, inserted);
        return inserted;
    }

    public int insertOverRegulator(IFluidHandler handler, CoverFluidRegulator regulator, boolean isOnPipe, FluidStack stack, boolean doFill, int allowed) {
        int rate = regulator.getTransferAmount();
        int count;
        switch (regulator.getTransferMode()) {
            case TRANSFER_ANY:
                return insert(handler, stack, doFill, allowed);
            case KEEP_EXACT:
                count = rate - countStack(handler, stack, regulator);
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

    public Tuple<CoverPump, Boolean> getCoverAtPipe(BlockPos pipePos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pipePos);
        if (tile instanceof TileEntityFluidPipe) {
            ICoverable coverable = ((TileEntityFluidPipe) tile).getCoverableImplementation();
            CoverBehavior cover = coverable.getCoverAtSide(handlerFacing);
            if (cover instanceof CoverPump) return new Tuple<>((CoverPump) cover, true);
        }
        tile = pipe.getWorld().getTileEntity(pipePos.offset(handlerFacing));
        if (tile != null) {
            ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, handlerFacing.getOpposite());
            if (coverable == null) return null;
            CoverBehavior cover = coverable.getCoverAtSide(handlerFacing.getOpposite());
            if (cover instanceof CoverPump) return new Tuple<>((CoverPump) cover, false);
        }
        return null;
    }

    public boolean exportsToPipe(Tuple<CoverPump, Boolean> tuple) {
        return tuple != null && (tuple.getSecond() ?
                tuple.getFirst().getPumpMode() == CoverPump.PumpMode.IMPORT :
                tuple.getFirst().getPumpMode() == CoverPump.PumpMode.EXPORT);
    }

    private int checkTransferable(TileEntityFluidPipeTickable pipe, int throughput, int amount, boolean doFill) {
        if (doFill)
            return Math.max(0, Math.min(throughput - pipe.getTransferredFluids(), amount));
        else
            return Math.max(0, Math.min(throughput - (pipe.getTransferredFluids() + simulatedTransfers), amount));
    }

    private void transfer(TileEntityFluidPipeTickable pipe, boolean doFill, int amount) {
        if (doFill) {
            pipe.transferFluid(amount);
            net.setEmptyNetTimer(20);
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
            super(inv.getPipePos(), inv.getFaceToHandler(), inv.getDistance());
            this.handler = handler;
        }
    }
}
