package gregtech.common.pipelike.fluidpipe.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    private boolean isActive;
    private int transferredFluids = 0;
    private FluidStack[] fluids;
    private int[] emptyTimer;
    private int currentChannel;

    public TileEntityFluidPipeTickable() {
        this.currentChannel = -1;
    }

    @Override
    public void update() {
        getCoverableImplementation().update();
        transferredFluids = 0;
        for (int i = 0; i < getContainedFluids().length; i++) {
            if (emptyTimer[i] > 0 && --emptyTimer[i] == 0)
                emptyTank(i);
        }
    }

    public int getCurrentChannel() {
        return currentChannel;
    }

    public void transferFluid(int amount) {
        transferredFluids += amount;
    }

    public int getTransferredFluids() {
        return transferredFluids;
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0) return null;
        return getContainedFluids()[channel];
    }

    public FluidStack[] getContainedFluids() {
        if (fluids == null) {
            int tanks = getNodeData().tanks;
            this.fluids = new FluidStack[tanks];
            this.emptyTimer = new int[tanks];
            Arrays.fill(emptyTimer, 0);
        }
        return fluids;
    }

    public void setContainingFluid(FluidStack stack, int channel) {
        if (channel < 0) return;
        this.getContainedFluids()[channel] = stack;
        this.emptyTimer[channel] = 20;
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
        return c >= 0 && c < fluids.length;
    }

    /**
     * Finds a channel for the given fluid
     *
     * @param stack to find a channel fot
     * @return channel
     */
    public int findChannel(FluidStack stack) {
        if (getContainedFluids().length == 1) {
            FluidStack channelStack = fluids[0];
            return (channelStack == null || channelStack.amount <= 0 || channelStack.isFluidEqual(stack)) ? 0 : -1;
        }
        int emptyTank = -1;
        for (int i = fluids.length - 1; i >= 0; i--) {
            FluidStack channelStack = fluids[i];
            if (channelStack == null || channelStack.amount <= 0)
                emptyTank = i;
            else if (channelStack.isFluidEqual(stack))
                return i;
        }
        return emptyTank;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("ActiveNode", isActive);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getContainedFluids().length; i++) {
            FluidStack stack1 = fluids[i];
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluidTag.setInteger("Timer", emptyTimer[i]);
            if (stack1 == null)
                fluidTag.setBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.setTag("Fluids", list);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isActive = nbt.getBoolean("ActiveNode");
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        fluids = new FluidStack[list.tagCount()];
        emptyTimer = new int[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            emptyTimer[i] = tag.getInteger("Timer");
            if (!tag.getBoolean("isNull"))
                fluids[i] = FluidStack.loadFluidStackFromNBT(tag);
        }
    }
}
