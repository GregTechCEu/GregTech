package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTFluidUtils;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public void update() {
        getCoverableImplementation().update();
        if (isActive) {
            pushFluidsFromTank(this);
        }
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

    public static void pushFluidsFromTank(IPipeTile<FluidPipeType, FluidPipeProperties> pipeTile) {
        PooledMutableBlockPos blockPos = PooledMutableBlockPos.retain();
        BlockFluidPipe blockFluidPipe = (BlockFluidPipe) pipeTile.getPipeBlock();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!pipeTile.isConnectionOpenVisual(side)) {
                continue; //do not dispatch energy to blocked sides
            }
            blockPos.setPos(pipeTile.getPipePos()).move(side);
            if (!pipeTile.getPipeWorld().isBlockLoaded(blockPos)) {
                continue; //do not allow cables to load chunks
            }
            TileEntity tileEntity = pipeTile.getPipeWorld().getTileEntity(blockPos);
            if (tileEntity == null) {
                continue; //do not emit into multiparts or other fluid pipes
            }
            IFluidHandler sourceHandler = pipeTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            IFluidHandler receiverHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
            if (sourceHandler != null && receiverHandler != null && blockFluidPipe.canPushIntoFluidHandler(pipeTile, tileEntity, sourceHandler, receiverHandler)) {
                GTFluidUtils.transferFluids(sourceHandler, receiverHandler, Integer.MAX_VALUE);
            }
        }
        blockPos.release();
    }

}
