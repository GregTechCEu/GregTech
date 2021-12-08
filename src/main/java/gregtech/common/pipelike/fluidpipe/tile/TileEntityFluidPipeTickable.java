package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    private List<Pair<IFluidHandler, Predicate<FluidStack>>> handlers;

    private List<Pair<IFluidHandler, Predicate<FluidStack>>> getHandlers() {
        return handlers == null ? handlers = getNeighbourHandlers() : handlers;
    }

    public int distribute(FluidStack stack) {
        List<Pair<IFluidHandler, Predicate<FluidStack>>> handlers = getHandlers();
        handlers.removeIf(pair -> !pair.getValue().test(stack) || pair.getKey().fill(stack, false) <= 0);
        if (handlers.size() == 0)
            return 0;

        int amountToDistribute = stack.amount;
        int c = amountToDistribute / handlers.size();
        int m = c == 0 ? amountToDistribute % handlers.size() : 0;
        int inserted = 0;
        for (Pair<IFluidHandler, Predicate<FluidStack>> pair : handlers) {
            FluidStack stackToFill = stack.copy();
            stackToFill.amount = c;
            if (m > 0) {
                stackToFill.amount++;
                m--;
            }
            inserted += pair.getKey().fill(stackToFill, true);
        }
        return inserted;
    }

    @Override
    public void update() {
        handlers = null;
        getCoverableImplementation().update();
        if (!world.isRemote && world.getTotalWorldTime() % FREQUENCY == 0) {
            FluidPipeNet net = getFluidPipeNet();
            List<Pair<IFluidHandler, Predicate<FluidStack>>> handlersO = getHandlers();
            if (handlersO.size() == 0) return;
            for (FluidTank tank : getFluidTanks()) {
                FluidStack stack = net.getFluidStack(tank.getFluid());
                if (stack != null && stack.amount > 0) {
                    List<Pair<IFluidHandler, Predicate<FluidStack>>> handlers = new ArrayList<>(handlersO);
                    handlers.removeIf(pair -> !pair.getValue().test(stack) || pair.getKey().fill(stack, false) <= 0);
                    if (handlers.size() == 0)
                        continue;

                    net.requestFluidForPipe(stack, this);


                    /*int amountToDistribute = Math.min(stack.amount, getCapacityPerTank() / 2);
                    int c = amountToDistribute / handlers.size();
                    int m = c == 0 ? amountToDistribute % handlers.size() : 0;
                    int inserted = 0;
                    for (Pair<IFluidHandler, Predicate<FluidStack>> pair : handlers) {
                        FluidStack stackToFill = stack.copy();
                        stackToFill.amount = c;
                        if (m > 0) {
                            stackToFill.amount++;
                            m--;
                        }
                        inserted += pair.getKey().fill(stackToFill, true);
                    }
                    FluidStack toDrain = stack.copy();
                    toDrain.amount = inserted;
                    net.drain(toDrain, getPos(), false, true);*/
                }
            }
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

}
