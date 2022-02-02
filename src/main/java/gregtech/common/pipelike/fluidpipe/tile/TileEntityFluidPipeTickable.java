package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverFluidFilter;
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
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable, IDataInfoProvider {

    public byte mLastReceivedFrom = 0, oLastReceivedFrom = 0;
    private PipeTankList pipeTankList;
    private final EnumMap<EnumFacing, PipeTankList> tankLists = new EnumMap<>(EnumFacing.class);
    private FluidTank[] fluidTanks;
    private long timer = 0L;
    private final int offset = GTValues.RNG.nextInt(20);

    public long getOffsetTimer() {
        return timer + offset;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            PipeTankList tankList = getTankList(facing);
            if (tankList == null)
                return null;
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankList);
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public void update() {
        timer++;
        getCoverableImplementation().update();
        if (!world.isRemote && getOffsetTimer() % FREQUENCY == 0) {
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
        List<Pair<IFluidHandler, Integer>> tanks = new ArrayList<>();
        int amount = fluid.amount;

        FluidStack maxFluid = fluid.copy();
        double availableCapacity = 0;

        for (byte aSide, i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
            // Get a list of tanks accepting fluids, and what side they're on
            aSide = (byte) ((i + j) % 6);
            EnumFacing facing = EnumFacing.VALUES[aSide];
            if (!isConnected(facing) || (mLastReceivedFrom & (1 << aSide)) != 0)
                continue;
            EnumFacing oppositeSide = facing.getOpposite();

            IFluidHandler fluidHandler = getFluidHandlerAt(fluid, facing, oppositeSide);
            if (fluidHandler == null)
                continue;

            int filled = fluidHandler.fill(maxFluid, false);

            if (filled > 0) {
                tanks.add(MutablePair.of(fluidHandler, filled));
                availableCapacity += filled;
            }
            maxFluid.amount = amount; // Because some mods do actually modify input fluid stack
        }

        if (availableCapacity <= 0)
            return;

        // How much of this fluid is available for distribution?
        final double maxAmount = Math.min(getCapacityPerTank() / 2, fluid.amount);

        // Now distribute
        for (Pair<IFluidHandler, Integer> pair : tanks) {
            if (availableCapacity > maxAmount)
                pair.setValue((int) Math.floor(pair.getValue() * maxAmount / availableCapacity)); // Distribue fluids based on percentage available space at destination
            if (pair.getValue() == 0)
                pair.setValue(1); // If the percent is not enough to give at least 1L, try to give 1L
            else if (pair.getValue() < 0)
                continue;

            FluidStack toInsert = fluid.copy();
            toInsert.amount = maxExtractable(tank, pair.getValue());

            if (toInsert.amount == 0)
                break;

            int inserted = pair.getKey().fill(toInsert, true);
            if (inserted > 0) {
                extract(tank, inserted);
            }
        }

    }

    private int maxExtractable(FluidTank tank, int toExtract) {
        FluidStack fluid = tank.getFluid();
        if (fluid == null || fluid.amount <= 0)
            return 0;
        return Math.min(toExtract, fluid.amount);
    }

    private int extract(FluidTank tank, int amount) {
        FluidStack fluid = tank.getFluid();
        if (fluid == null || fluid.amount <= 0)
            return 0;
        int extracted = Math.min(amount, fluid.amount);
        fluid.amount -= extracted;
        if (fluid.amount == 0) {
            tank.setFluid(null);
        }
        return extracted;
    }

    private IFluidHandler getFluidHandlerAt(FluidStack fluid, EnumFacing facing, EnumFacing oppositeSide) {
        TileEntity tile = world.getTileEntity(pos.offset(facing));
        if (tile == null)
            return null;
        IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, oppositeSide);
        if (fluidHandler == null)
            return null;

        CoverBehavior cover = getCoverableImplementation().getCoverAtSide(facing);
        if (cover instanceof CoverFluidFilter && !((CoverFluidFilter) cover).testFluidStack(fluid))
            return null;
        ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, oppositeSide);
        if (coverable == null)
            return fluidHandler;
        cover = coverable.getCoverAtSide(oppositeSide);
        if (cover instanceof CoverFluidFilter && !((CoverFluidFilter) cover).testFluidStack(fluid))
            return null;
        return fluidHandler;
    }

    public void receivedFrom(EnumFacing facing) {
        if (facing != null)
            mLastReceivedFrom |= (1 << facing.getIndex());
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0 || channel >= getFluidTanks().length) return null;
        return getFluidTanks()[channel].getFluid();
    }

    private void createTanksList() {
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, null, fluidTanks);
        for (EnumFacing facing : EnumFacing.VALUES) {
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
        return tankLists.getOrDefault(facing, pipeTankList);
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
