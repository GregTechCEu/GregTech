package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    @Override
    public void update() {
        getCoverableImplementation().update();
        Iterator<Map.Entry<EnumFacing, Integer>> iterator = getLastInserted().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EnumFacing, Integer> entry = iterator.next();
            if (entry.getValue() - 1 == 0)
                iterator.remove();
            else
                entry.setValue(entry.getValue() - 1);
        }

        if (!world.isRemote && world.getTotalWorldTime() % FREQUENCY == 0) {
            FluidPipeNet net = getFluidPipeNet();
            List<Pair<IFluidHandler, Predicate<FluidStack>>> handlersO = getNeighbourHandlers();
            if (handlersO.size() == 0) return;
            for (FluidTank tank : getFluidTanks()) {
                FluidStack stack = net.getFluidStack(tank.getFluid());
                if (stack != null && stack.amount > 0) {
                    List<Pair<IFluidHandler, Predicate<FluidStack>>> handlers = new ArrayList<>(handlersO);
                    handlers.removeIf(pair -> !pair.getValue().test(stack) || pair.getKey().fill(stack, false) <= 0);
                    if (handlers.size() == 0)
                        continue;
                    int amountToDistribute = Math.min(stack.amount, getCapacityPerTank() / 2);
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
                    net.drain(toDrain, getPos(), false, true);
                }
            }
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

}
