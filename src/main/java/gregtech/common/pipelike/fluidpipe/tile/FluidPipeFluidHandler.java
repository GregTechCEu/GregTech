package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperty;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class FluidPipeFluidHandler implements IFluidHandler {

    private final IPipeTile<FluidPipeType, FluidPipeProperty> pipeTile;
    private WeakReference<FluidPipeNet> currentPipeNet = new WeakReference<>(null);

    public FluidPipeFluidHandler(IPipeTile<FluidPipeType, FluidPipeProperty> pipeTile) {
        this.pipeTile = pipeTile;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        FluidTank fluidTank = getNetFluidTank();
        return fluidTank == null ? new IFluidTankProperties[0] : fluidTank.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        FluidTank fluidTank = getNetFluidTank();
        return fluidTank == null ? 0 : fluidTank.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidTank fluidTank = getNetFluidTank();
        return fluidTank == null ? null : fluidTank.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidTank fluidTank = getNetFluidTank();
        return fluidTank == null ? null : fluidTank.drain(maxDrain, doDrain);
    }

    protected FluidTank getNetFluidTank() {
        FluidPipeNet fluidPipeNet = getFluidPipeNet();
        return fluidPipeNet == null ? null : fluidPipeNet.getFluidNetTank();
    }

    public FluidPipeNet getFluidPipeNet() {
        FluidPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() &&
            currentPipeNet.containsNode(pipeTile.getPipePos()))
            return currentPipeNet; //if current net is valid and does contain position, return it
        WorldFluidPipeNet worldFluidPipeNet = (WorldFluidPipeNet) pipeTile.getPipeBlock().getWorldPipeNet(pipeTile.getPipeWorld());
        currentPipeNet = worldFluidPipeNet.getNetFromPos(pipeTile.getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

}
