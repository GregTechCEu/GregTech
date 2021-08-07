package gregtech.common.pipelike.fluidpipe.net;

import com.google.common.base.Preconditions;
import gregtech.api.util.PerTickIntCounter;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidNetTank extends FluidTank {

    private final FluidPipeNet handle;
    private final PerTickIntCounter drainedThisTick = new PerTickIntCounter(0);

    public FluidNetTank(FluidPipeNet handle) {
        super(0);
        this.handle = handle;
    }

    private int getMaxThroughput() {
        return handle.getMaxThroughput();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        Preconditions.checkNotNull(resource, "resource");
        FluidStack copyStack = resource.copy();
        copyStack.amount = Math.min(copyStack.amount, getMaxThroughput());
        FluidPipeProperties properties = handle.getNodeData();
        boolean isLeakingPipe = copyStack.getFluid().isGaseous(copyStack) && !properties.gasProof;
        boolean isBurningPipe = copyStack.getFluid().getTemperature(copyStack) > properties.maxFluidTemperature;
        if (isLeakingPipe || isBurningPipe) {
            handle.destroyNetwork(isLeakingPipe, isBurningPipe);
            return copyStack.amount;
        }
        return super.fill(copyStack, doFill);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        }
        int maxDrainLeftThisTick = getMaxThroughput() - drainedThisTick.get(handle.getWorldData());
        int originalAmount = resource.amount;
        resource.amount = Math.min(originalAmount, maxDrainLeftThisTick);
        FluidStack resultDrained = super.drain(resource, doDrain);
        resource.amount = originalAmount;
        if (resultDrained != null && doDrain) {
            drainedThisTick.increment(handle.getWorldData(), resultDrained.amount);
        }
        return resultDrained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        int maxDrainLeftThisTick = getMaxThroughput() - drainedThisTick.get(handle.getWorldData());
        maxDrain = Math.min(maxDrain, maxDrainLeftThisTick);
        if (maxDrain == 0) {
            return null;
        }
        FluidStack resultDrained = super.drain(maxDrain, doDrain);
        if (resultDrained != null && doDrain) {
            drainedThisTick.increment(handle.getWorldData(), resultDrained.amount);
        }
        return resultDrained;
    }

    public void updateTankCapacity(int newTankCapacity) {
        this.capacity = newTankCapacity;
        if (this.fluid != null) {
            this.fluid.amount = Math.min(this.fluid.amount, newTankCapacity);
        }
    }

}
