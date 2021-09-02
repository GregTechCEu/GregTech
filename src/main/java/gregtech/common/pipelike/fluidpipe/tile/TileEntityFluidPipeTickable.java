package gregtech.common.pipelike.fluidpipe.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    @Override
    public void update() {
        getCoverableImplementation().update();
        if (world.isRemote)
            return;
        List<IFluidHandler> handlers = new ArrayList<>();
        for (EnumFacing facing : getOpenFaces()) {
            TileEntity tile = getWorld().getTileEntity(pos.offset(facing));
            if (tile == null) continue;
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
            if (handler != null) {
                handlers.add(handler);
            }
        }
        if(handlers.size() == 0) return;
        for (FluidTank tank : getFluidTanks()) {
            FluidStack stack = tank.getFluid();
            if (stack != null && stack.amount > 0) {
                int amountToDistribute = (int) Math.ceil(stack.amount / 2.0);
                int c = amountToDistribute / handlers.size();
                int m = amountToDistribute % handlers.size();
                for (IFluidHandler handler : handlers) {
                    int count = c;
                    if(m > 0) {
                        count++;
                        m--;
                    }
                    FluidStack stackToFill = stack.copy();
                    stackToFill.amount = count;
                    stackToFill.amount = handler.fill(stackToFill, true);
                    getTankList().drain(stackToFill, true);
                }
            }
        }

    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

}
