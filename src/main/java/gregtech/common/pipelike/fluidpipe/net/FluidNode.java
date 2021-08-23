package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.nodenet.Node;
import gregtech.api.pipenet.nodenet.NodeNet;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.PerTickIntCounter;
import gregtech.api.util.TickingObjectHolder;
import net.minecraftforge.fluids.FluidStack;

public class FluidNode extends Node<FluidPipeProperties> {

    private TickingObjectHolder<FluidStack>[] fluidHolders;
    private final PerTickIntCounter transferredFluids = new PerTickIntCounter(0);
    private int currentChannel = -1;

    public FluidNode(NodeNet<FluidPipeProperties> nodeNet, IPipeTile<?, FluidPipeProperties> pipe) {
        super(nodeNet, pipe);
    }

    public void transferFluid(int amount) {
        transferredFluids.increment(getWorld(), amount);
    }

    public int getTransferredFluids() {
        return transferredFluids.get(getWorld());
    }

    public int getCurrentChannel() {
        return currentChannel;
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0) return null;
        return getFluidHolders()[channel].getNullable(getWorld());
    }

    protected TickingObjectHolder<FluidStack>[] getFluidHolders() {
        if(fluidHolders == null) {
            this.fluidHolders = new TickingObjectHolder[getNodeData().tanks];
            for(int i = 0; i < fluidHolders.length; i++) {
                fluidHolders[i] = new TickingObjectHolder<>(null, 20);
            }
        }
        return fluidHolders;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidHolders().length];
        for(int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidHolders[i].getNullable(getWorld());
        }
        return fluids;
    }

    public void setContainingFluid(FluidStack stack, int channel) {
        if (channel < 0) return;
        getFluidHolders()[channel].reset(stack);
        this.currentChannel = -1;
    }

    private void emptyTank(int channel) {
        if (channel < 0) return;
        this.getContainedFluids()[channel] = null;
    }

    public boolean areTanksEmpty() {
        for (FluidStack fluidStack : getContainedFluids())
            if (fluidStack != null)
                return false;
        return true;
    }

    public boolean findAndSetChannel(FluidStack stack) {
        int c = findChannel(stack);
        this.currentChannel = c;
        return c >= 0 && c < fluidHolders.length;
    }

    /**
     * Finds a channel for the given fluid
     *
     * @param stack to find a channel fot
     * @return channel
     */
    public int findChannel(FluidStack stack) {
        if (getFluidHolders().length == 1) {
            FluidStack channelStack = getContainedFluid(0);
            return (channelStack == null || /*channelStack.amount <= 0 || */channelStack.isFluidEqual(stack)) ? 0 : -1;
        }
        int emptyTank = -1;
        for (int i = fluidHolders.length - 1; i >= 0; i--) {
            FluidStack channelStack = getContainedFluid(i);
            if (channelStack == null/* || channelStack.amount <= 0*/)
                emptyTank = i;
            else if (channelStack.isFluidEqual(stack))
                return i;
        }
        return emptyTank;
    }
}
