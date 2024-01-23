package gregtech.common.pipelike.fluidpipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    @Override
    public void update() {
        // timer++;
        getCoverableImplementation().update();
        // if (!world.isRemote && getOffsetTimer() % FREQUENCY == 0) {
        // lastReceivedFrom &= 63;
        // if (lastReceivedFrom == 63) {
        // lastReceivedFrom = 0;
        // }
        //
        // boolean shouldDistribute = (oldLastReceivedFrom == lastReceivedFrom);
        // int tanks = getNodeData().getTanks();
        // for (int i = 0, j = GTValues.RNG.nextInt(tanks); i < tanks; i++) {
        // int index = (i + j) % tanks;
        // FluidTank tank = getFluidTanks()[index];
        // FluidStack fluid = tank.getFluid();
        // if (fluid == null)
        // continue;
        // if (fluid.amount <= 0) {
        // tank.setFluid(null);
        // continue;
        // }
        //
        // if (shouldDistribute) {
        // distributeFluid(index, tank, fluid);
        // lastReceivedFrom = 0;
        // }
        // }
        // oldLastReceivedFrom = lastReceivedFrom;
        // }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    // private void distributeFluid(int channel, FluidTank tank, FluidStack fluid) {
    // // Tank, From, Amount to receive
    // List<FluidTransaction> tanks = new ArrayList<>();
    // int amount = fluid.amount;
    //
    // FluidStack maxFluid = fluid.copy();
    // double availableCapacity = 0;
    //
    // for (byte i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
    // // Get a list of tanks accepting fluids, and what side they're on
    // byte side = (byte) ((i + j) % 6);
    // EnumFacing facing = EnumFacing.VALUES[side];
    //
    // if (!isConnected(facing) || (lastReceivedFrom & (1 << side)) != 0) {
    // continue;
    // }
    //
    // TileEntity neighbor = getNeighbor(facing);
    // if (neighbor == null) continue;
    // IFluidHandler fluidHandler = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
    // facing.getOpposite());
    // if (fluidHandler == null) continue;
    //
    // IFluidHandler pipeTank = tank;
    // Cover cover = getCoverableImplementation().getCoverAtSide(facing);
    //
    // // pipeTank should only be determined by the cover attached to the actual pipe
    // if (cover != null) {
    // pipeTank = cover.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, pipeTank);
    // // Shutter covers return null capability when active, so check here to prevent NPE
    // if (pipeTank == null || checkForPumpCover(cover)) continue;
    // } else {
    // CoverableView coverable = neighbor.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
    // facing.getOpposite());
    // if (coverable != null) {
    // cover = coverable.getCoverAtSide(facing.getOpposite());
    // if (checkForPumpCover(cover)) continue;
    // }
    // }
    //
    // FluidStack drainable = pipeTank.drain(maxFluid, false);
    // if (drainable == null || drainable.amount <= 0) {
    // continue;
    // }
    //
    // int filled = Math.min(fluidHandler.fill(maxFluid, false), drainable.amount);
    //
    // if (filled > 0) {
    // tanks.add(new FluidTransaction(fluidHandler, pipeTank, filled));
    // availableCapacity += filled;
    // }
    // maxFluid.amount = amount; // Because some mods do actually modify input fluid stack
    // }
    //
    // if (availableCapacity <= 0)
    // return;
    //
    // // How much of this fluid is available for distribution?
    // final double maxAmount = Math.min(getCapacityPerTank() / 2, fluid.amount);
    //
    // // Now distribute
    // for (FluidTransaction transaction : tanks) {
    // if (availableCapacity > maxAmount) {
    // transaction.amount = (int) Math.floor(transaction.amount * maxAmount / availableCapacity); // Distribute
    // // fluids
    // // based on
    // // percentage
    // // available
    // // space at
    // // destination
    // }
    // if (transaction.amount == 0) {
    // if (tank.getFluidAmount() <= 0) break; // If there is no more stored fluid, stop transferring to prevent
    // // dupes
    // transaction.amount = 1; // If the percent is not enough to give at least 1L, try to give 1L
    // } else if (transaction.amount < 0) {
    // continue;
    // }
    //
    // FluidStack toInsert = fluid.copy();
    // toInsert.amount = transaction.amount;
    //
    // int inserted = transaction.target.fill(toInsert, true);
    // if (inserted > 0) {
    // transaction.pipeTank.drain(inserted, true);
    // }
    // }
    // }

    // private boolean checkForPumpCover(@Nullable Cover cover) {
    // if (cover instanceof CoverPump coverPump) {
    // int pipeThroughput = getNodeData().getThroughput() * 20;
    // if (coverPump.getTransferRate() > pipeThroughput) {
    // coverPump.setTransferRate(pipeThroughput);
    // }
    // return coverPump.getManualImportExportMode() == ManualImportExportMode.DISABLED;
    // }
    // return false;
    // }
    //
    // private IFluidHandler getFluidHandlerAt(EnumFacing facing, EnumFacing oppositeSide) {
    // TileEntity tile = world.getTileEntity(pos.offset(facing));
    // if (tile == null) {
    // return null;
    // }
    // return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, oppositeSide);
    // }
}
