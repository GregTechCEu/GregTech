package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.util.GTLog;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    private long time = 0;

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
            List<IFluidHandler> handlers = getNeighbourHandlers();
            if (handlers.size() == 0) return;
            for (FluidTank tank : getFluidTanks()) {
                FluidStack stack = tank.getFluid();
                if (stack != null && stack.amount > 0) {
                    int amountToDistribute = getCapacityPerTank() / 2;
                    if (stack.amount < amountToDistribute) {
                        GTLog.logger.info("Not enough fluid. Requesting...");
                        getFluidPipeNet().requestFluid(this, stack);
                        continue;
                    }
                    int c = amountToDistribute / handlers.size();
                    int m = amountToDistribute % handlers.size();
                    int inserted = 0;
                    for (IFluidHandler handler : handlers) {
                        FluidStack stackToFill = stack.copy();
                        stackToFill.amount = c;
                        if (m > 0) {
                            stackToFill.amount++;
                            m--;
                        }
                        inserted += handler.fill(stackToFill, true);
                    }
                    FluidStack toDrain = stack.copy();
                    toDrain.amount = inserted;
                    getTankList().drain(toDrain, true);
                }
            }
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

}
