package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.GTFluidUtils;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private int transferredFluids = 0;

    @Override
    public void update() {
        getCoverableImplementation().update();
        transferredFluids = 0;
    }

    public void transferFluid(int amount) {
        transferredFluids += amount;
    }

    public int getTransferredFluids() {
        return transferredFluids;
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("ActiveNode", isActive);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.isActive = compound.getBoolean("ActiveNode");
    }
}
