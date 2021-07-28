package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.util.GTLog;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
        return new IFluidTankProperties[]{new FluidTankProperties(null, getCapacity(), true, false)};
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
        return insertFirst(createHandlers(), resource, doFill);
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

    protected int insertFirst(List<Handler> handlers, FluidStack stack, boolean doFill) {
        int toInsert = stack.amount;
        for (Handler handler : handlers) {
            toInsert = insert(handler, stack, doFill, toInsert);
            if (toInsert == 0)
                return 0;
        }
        return toInsert;
    }

    private int insert(Handler handler, FluidStack stack, boolean doFill, int max) {
        int amount = Math.min(stack.amount, max);
        amount = checkTransferable(pipe, pipe.getNodeData().throughput * 20, amount, doFill);
        FluidStack toInsert = stack.copy();
        toInsert.amount = amount;
        log.info(" - inserting {}, doFill {}, max {}, amount {}", stack, doFill, max, amount);
        int inserted = handler.handler.fill(toInsert, doFill);
        if (inserted > 0) {
            transfer(pipe, doFill, inserted);
        }
        log.info(" - inserted {}", inserted);
        return inserted;
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
