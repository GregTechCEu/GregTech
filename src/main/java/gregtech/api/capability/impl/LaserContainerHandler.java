package gregtech.api.capability.impl;


import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.common.pipelike.laser.tile.CableLaserContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Predicate;

public class LaserContainerHandler extends MTETrait implements LaserContainer{

    private final long maxCapacity;
    private long LaserStored;

    private final long maxInputLaser;
    private final long maxInputParallel;

    private final long maxOutputLaser;
    private final long maxOutputParallel;

    private Predicate<EnumFacing> sideInputCondition;
    private Predicate<EnumFacing> sideOutputCondition;

    public LaserContainerHandler(MetaTileEntity tileEntity, long maxCapacity, long maxInputLaser, long maxInputParallel, long maxOutputLaser, long maxOutputParallel) {
        super(tileEntity);
        this.maxCapacity = maxCapacity;
        this.maxInputLaser = maxInputLaser;
        this.maxInputParallel = maxInputParallel;
        this.maxOutputLaser = maxOutputLaser;
        this.maxOutputParallel = maxOutputParallel;
    }
    public void setSideInputCondition(Predicate<EnumFacing> sideInputCondition) {
        this.sideInputCondition = sideInputCondition;
    }

    public void setSideOutputCondition(Predicate<EnumFacing> sideOutputCondition) {
        this.sideOutputCondition = sideOutputCondition;
    }
    public static LaserContainerHandler LaseremitterContainer(MetaTileEntity tileEntity, long maxCapacity, long maxOutputVoltage, long maxOutputAmperage) {
        return new LaserContainerHandler(tileEntity, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static LaserContainerHandler LaserreceiverContainer(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage) {
        return new LaserContainerHandler(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }
    @Override
    public String getName() {
        return "LaserContainer";
    }

    @Override
    public int getNetworkID() {
        return 15;
    }


    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability ==GregtechTileCapabilities.LASER_CAPABILITY) {
            return (T) this;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("LaserStored", LaserStored);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        this.LaserStored = compound.getLong("LaserStored");
        notifyEnergyListener(true);
    }

    protected void notifyEnergyListener(boolean isInitialChange) {
        if (metaTileEntity instanceof ILaserListener) {
            ((ILaserListener) metaTileEntity).onEnergyChanged(this, isInitialChange);
        }
    }

    public long getLaserStored() {
        return this.LaserStored;
    }

    public void setLaserStored(long LaserStored) {
        this.LaserStored = LaserStored;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            notifyEnergyListener(false);
        }
    }
    @Override
    public void update() {
        if (getMetaTileEntity().getWorld().isRemote)
            return;
        if (getLaserStored() >= getOutputLaser() && getOutputLaser() > 0 && getOutputParallel() > 0) {
            long outputLaser = getOutputLaser();
            long outputParallel = Math.min(getOutputLaser() / outputLaser, getOutputParallel());
            if (outputParallel == 0) return;
            long currentParallel = 0;
            for (EnumFacing side : EnumFacing.VALUES) {
                if (!outputsLaser(side)) continue;
                TileEntity tileEntity = metaTileEntity.getWorld().getTileEntity(metaTileEntity.getPos().offset(side));
                EnumFacing oppositeSide = side.getOpposite();
                if (tileEntity != null && tileEntity.hasCapability(GregtechTileCapabilities.LASER_CAPABILITY, oppositeSide)) {
                    LaserContainer laserContainer = tileEntity.getCapability(GregtechTileCapabilities.LASER_CAPABILITY, oppositeSide);
                    if (laserContainer == null || !laserContainer.inputsLaser(oppositeSide)) continue;
                    currentParallel += laserContainer.acceptLaserFromNetwork(oppositeSide, outputLaser, outputParallel - currentParallel);
                    if (currentParallel == outputParallel) break;
                }
            }
            if (currentParallel > 0) {
                setLaserStored(getLaserStored() - currentParallel * outputLaser);
            }
        }
    }
    @Override
    public long acceptLaserFromNetwork(EnumFacing side, long Laser, long parallel) {
        long canAccept = getLaserCapacity() - getLaserStored();
        if (Laser > 0L && parallel > 0L && (side == null || inputsLaser(side))) {
            if (Laser > getInputLaser()) {
                return Math.min(parallel, getInputParallel());
            }
            if (canAccept >= Laser) {
                long parallelAccepted = Math.min(canAccept / Laser, Math.min(parallel, getInputParallel()));
                if (parallelAccepted > 0) {
                    setLaserStored(getLaserStored() + Laser * parallelAccepted);
                    return parallelAccepted;
                }
            }
        }
        return 0;
    }
    @Override
    public long getLaserCapacity() {
        return this.maxCapacity;
    }

    @Override
    public boolean inputsLaser(EnumFacing side) {
        return !outputsLaser(side) && getInputLaser() > 0 && (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsLaser(EnumFacing side) {
        return getOutputLaser() > 0 && (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long changeLaser(long energyToAdd) {
        long oldLaserStored = getLaserStored();
        long newLaserStored = (maxCapacity - oldLaserStored < energyToAdd) ? maxCapacity : (oldLaserStored + energyToAdd);
        if (newLaserStored < 0)
            newLaserStored = 0;
        setLaserStored(newLaserStored);
        return newLaserStored - oldLaserStored;
    }

    @Override
    public long getOutputLaser() {
        return this.maxOutputLaser;
    }

    @Override
    public long getOutputParallel() {
        return this.maxOutputParallel;
    }

    @Override
    public long getInputParallel() {
        return this.maxInputParallel;
    }

    @Override
    public long getInputLaser() {
        return this.maxInputLaser;
    }
}
interface ILaserListener {
    void onEnergyChanged(LaserContainerHandler container, boolean isInitialChange);
}
