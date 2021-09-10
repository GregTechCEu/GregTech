package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.common.covers.CoverPump;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
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

        if (!world.isRemote && ++time % 5 == 0) {
            List<IFluidHandler> handlers = new ArrayList<>();
            for (EnumFacing facing : getOpenFaces()) {
                if (getLastInserted().containsKey(facing))
                    continue;
                TileEntity tile = getWorld().getTileEntity(pos.offset(facing));
                if (tile == null) continue;
                IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                if (handler != null) {
                    ICoverable coverable = getCoverableImplementation();
                    CoverBehavior cover = coverable.getCoverAtSide(facing);
                    if (cover instanceof CoverPump && ((CoverPump) cover).getPumpMode() == CoverPump.PumpMode.IMPORT && ((CoverPump) cover).blocksInput()) {
                        continue;
                    }
                    coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing.getOpposite());
                    if (coverable != null) {
                        cover = coverable.getCoverAtSide(facing.getOpposite());
                        if (cover instanceof CoverPump && ((CoverPump) cover).getPumpMode() == CoverPump.PumpMode.EXPORT && ((CoverPump) cover).blocksInput()) {
                            continue;
                        }
                    }
                    handlers.add(handler);
                }
            }
            if (handlers.size() == 0) return;
            for (FluidTank tank : getFluidTanks()) {
                FluidStack stack = tank.getFluid();
                if (stack != null && stack.amount > 0) {
                    int amountToDistribute = Math.min(stack.amount, getCapacityPerTank() / 2);
                    int c = amountToDistribute / handlers.size();
                    int m = amountToDistribute % handlers.size();
                    for (IFluidHandler handler : handlers) {
                        int count = c;
                        if (m > 0) {
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
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

}
