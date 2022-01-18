package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.PipeTankList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.MutableTriple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable, IDataInfoProvider {

    public byte mLastReceivedFrom = 0, oLastReceivedFrom = 0;
    private PipeTankList pipeTankList;
    private EnumMap<EnumFacing, PipeTankList> tankLists = new EnumMap<>(EnumFacing.class);
    private FluidTank[] fluidTanks;

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            PipeTankList tankList = getTankList(facing);
            if(facing == null || tankList == null)
                return null;
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankList);
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public void update() {
        getCoverableImplementation().update();
        if (!world.isRemote && world.getTotalWorldTime() % FREQUENCY == 0) {
            FluidPipeNet net = getFluidPipeNet();

            mLastReceivedFrom &= 63;
            if (mLastReceivedFrom == 63) {
                mLastReceivedFrom = 0;
            }

            boolean shouldDistribute = (oLastReceivedFrom == mLastReceivedFrom);
            int tanks = getNodeData().getTanks();
            for (int i = 0, j = GTValues.RNG.nextInt(tanks); i < tanks; i++) {
                int index = (i + j) % tanks;
                FluidTank tank = getFluidTanks()[index];
                FluidStack fluid = tank.getFluid();
                if (fluid == null)
                    continue;
                if (fluid.amount <= 0) {
                    tank.setFluid(null);
                    continue;
                }

                //if (checkEnvironment(index, aBaseMetaTileEntity)) return;

                if (shouldDistribute) {
                    distributeFluid(index, tank, fluid);
                    mLastReceivedFrom = 0;
                }
            }
            oLastReceivedFrom = mLastReceivedFrom;

        }

    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    private void distributeFluid(int channel, FluidTank tank, FluidStack fluid) {
        // Tank, From, Amount to receive
        List<MutableTriple<IFluidHandler, EnumFacing, Integer>> tTanks = new ArrayList<>();
        int amount = fluid.amount;

        for (byte aSide, i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
            // Get a list of tanks accepting fluids, and what side they're on
            aSide = (byte) ((i + j) % 6);
            EnumFacing facing = EnumFacing.VALUES[aSide];
            if (!isConnectionOpenAny(facing))
                continue;
            EnumFacing oppositeSide = facing.getOpposite();

            TileEntity tile = world.getTileEntity(pos.offset(facing));
            if (tile == null)
                continue;
            IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, oppositeSide);
            if (fluidHandler == null)
                continue;

            if ((mLastReceivedFrom & (1 << aSide)) == 0 /*&& check for filters here*/) {
                if (fluidHandler.fill(fluid, false) > 0) {
                    tTanks.add(new MutableTriple<>(fluidHandler, oppositeSide, 0));
                }
                fluid.amount = amount; // Because some mods do actually modify input fluid stack
            }
        }

        // How much of this fluid is available for distribution?
        double tAmount = Math.max(1, Math.min(getCapacityPerTank() * 10, fluid.amount)), tNumTanks = tTanks.size();
        FluidStack maxFluid = fluid.copy();
        maxFluid.amount = Integer.MAX_VALUE;

        double availableCapacity = 0;
        // Calculate available capacity for distribution from all tanks
        for (MutableTriple<IFluidHandler, EnumFacing, Integer> tEntry : tTanks) {
            tEntry.right = tEntry.left.fill(maxFluid, false);
            availableCapacity += tEntry.right;
        }

        // Now distribute
        for (MutableTriple<IFluidHandler, EnumFacing, Integer> tEntry : tTanks) {
            if (availableCapacity > tAmount)
                tEntry.right = (int) Math.floor(tEntry.right * tAmount / availableCapacity); // Distribue fluids based on percentage available space at destination
            if (tEntry.right == 0)
                tEntry.right = (int) Math.min(1, tAmount); // If the percent is not enough to give at least 1L, try to give 1L
            if (tEntry.right <= 0) continue;

            int tFilledAmount = tEntry.left.fill(getTankList().drainFromIndex(tEntry.right, false, channel), false);

            if (tFilledAmount > 0)
                tEntry.left.fill(getTankList().drainFromIndex(tFilledAmount, true, channel), true);
        }

    }

    public void receivedFrom(EnumFacing facing) {
        mLastReceivedFrom |= (1 << facing.getIndex());
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0) return null;
        return getFluidTanks()[channel].getFluid();
    }

    public FluidStack findFluid(FluidStack stack) {
        return getContainedFluid(findChannel(stack));
    }

    private void createTanksList() {
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, null, fluidTanks);
        for(EnumFacing facing : EnumFacing.VALUES) {
            tankLists.put(facing, new PipeTankList(this, facing, fluidTanks));
        }
    }

    public PipeTankList getTankList() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return pipeTankList;
    }

    public PipeTankList getTankList(EnumFacing facing) {
        if (tankLists.isEmpty() || fluidTanks == null) {
            createTanksList();
        }
        return tankLists.get(facing);
    }

    public FluidTank[] getFluidTanks() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return fluidTanks;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidTanks().length];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidTanks[i].getFluid();
        }
        return fluids;
    }

    /**
     * Finds a channel for the given fluid
     *
     * @param stack to find a channel fot
     * @return channel
     */
    public int findChannel(FluidStack stack) {
        if (getFluidTanks().length == 1) {
            FluidStack channelStack = getContainedFluid(0);
            return (channelStack == null || channelStack.amount <= 0 || channelStack.isFluidEqual(stack)) ? 0 : -1;
        }
        int emptyTank = -1;
        for (int i = fluidTanks.length - 1; i >= 0; i--) {
            FluidStack channelStack = getContainedFluid(i);
            if (channelStack == null || channelStack.amount <= 0)
                emptyTank = i;
            else if (channelStack.isFluidEqual(stack))
                return i;
        }
        return emptyTank;
    }

    public int setContainingFluid(FluidStack stack, int channel, boolean fill) {
        if (channel < 0)
            return stack == null ? 0 : stack.amount;
        if (stack == null || stack.amount <= 0) {
            getFluidTanks()[channel].setFluid(null);
            return 0;
        }
        FluidTank tank = getFluidTanks()[channel];
        FluidStack currentStack = tank.getFluid();
        if (currentStack == null || currentStack.amount <= 0) {
            checkAndDestroy(stack);
        } else if (fill) {
            int toFill = stack.amount;
            if (toFill + currentStack.amount > tank.getCapacity())
                toFill = tank.getCapacity() - currentStack.amount;
            currentStack.amount += toFill;
            return toFill;
        }
        stack.amount = Math.min(stack.amount, tank.getCapacity());
        tank.setFluid(stack);
        return stack.amount;
    }

    public boolean areTanksEmpty() {
        for (FluidStack fluidStack : getContainedFluids())
            if (fluidStack != null) {
                if (fluidStack.amount <= 0) {
                    setContainingFluid(null, findChannel(fluidStack), false);
                    continue;
                }
                return false;
            }
        return true;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getFluidTanks().length; i++) {
            FluidStack stack1 = getContainedFluid(i);
            NBTTagCompound fluidTag = new NBTTagCompound();
            if (stack1 == null || stack1.amount <= 0)
                fluidTag.setBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.setTag("Fluids", list);
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        createTanksList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if (!tag.getBoolean("isNull")) {
                fluidTanks[i].setFluid(FluidStack.loadFluidStackFromNBT(tag));
            }
        }
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        FluidStack[] fluids = this.getContainedFluids();
        if (fluids != null) {
            boolean allTanksEmpty = true;
            for (int i = 0; i < fluids.length; i++) {
                if (fluids[i] != null) {
                    if (fluids[i].getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation("behavior.tricorder.tank", i,
                            new TextComponentTranslation(GTUtility.formatNumbers(fluids[i].amount)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(GTUtility.formatNumbers(this.getCapacityPerTank())).setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            new TextComponentTranslation(fluids[i].getFluid().getLocalizedName(fluids[i])).setStyle(new Style().setColor(TextFormatting.GOLD))
                    ));
                }
            }

            if (allTanksEmpty)
                list.add(new TextComponentTranslation("behavior.tricorder.tanks_empty"));
        }
        return list;
    }
}
