package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class LaserContainerHandler extends EnergyContainerHandler implements ILaserContainer {

    public LaserContainerHandler(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage,
                                 long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    public static LaserContainerHandler emitterContainer(MetaTileEntity tileEntity, long maxCapacity,
                                                         long maxOutputVoltage, long maxOutputAmperage) {
        return new LaserContainerHandler(tileEntity, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static LaserContainerHandler receiverContainer(MetaTileEntity tileEntity, long maxCapacity,
                                                          long maxInputVoltage, long maxInputAmperage) {
        return new LaserContainerHandler(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    public long receiveLaser(long laserVoltage, long laserAmperage) {
        if (getInputVoltage() == 0) return 0;
        long allowedAmps = getEnergyCanBeInserted() / laserVoltage;
        addEnergy(laserVoltage * allowedAmps);
        // over voltage explosion
        if (laserVoltage > getInputVoltage())
            getMetaTileEntity().doExplosion(GTUtility.getExplosionPower(laserVoltage));
        return allowedAmps;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(this);
        }
        return null;
    }

    @Override
    public void update() {
        amps = 0;
        if (getMetaTileEntity().getWorld().isRemote)
            return;
        if (metaTileEntity.getOffsetTimer() % 20 == 0) {
            lastEnergyOutputPerSec = energyOutputPerSec;
            lastEnergyInputPerSec = energyInputPerSec;
            energyOutputPerSec = 0;
            energyInputPerSec = 0;
        }
        if (getEnergyStored() >= getOutputVoltage() && getOutputVoltage() > 0 && getOutputAmperage() > 0) {
            long outputVoltage = getOutputVoltage();
            long outputAmperes = Math.min(getEnergyStored() / outputVoltage, getOutputAmperage());
            if (outputAmperes == 0) return;
            long amperesUsed = 0;
            for (EnumFacing side : EnumFacing.VALUES) {
                if (!outputsEnergy(side)) continue;
                TileEntity tileEntity = metaTileEntity.getWorld().getTileEntity(metaTileEntity.getPos().offset(side));
                EnumFacing oppositeSide = side.getOpposite();
                if (tileEntity != null &&
                        tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide)) {
                    ILaserRelay relay = tileEntity
                            .getCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide);
                    if (relay == null) continue;
                    amperesUsed += relay.receiveLaser(outputVoltage, outputAmperes - amperesUsed);
                    if (amperesUsed == outputAmperes) break;
                }
            }
            if (amperesUsed > 0) {
                setEnergyStored(getEnergyStored() - amperesUsed * outputVoltage);
            }
        }
    }

    @Override
    public String toString() {
        return "LaserContainerHandler{" +
                "maxCapacity=" + maxCapacity +
                ", energyStored=" + energyStored +
                ", maxInputVoltage=" + getInputVoltage() +
                ", maxInputAmperage=" + getInputAmperage() +
                ", maxOutputVoltage=" + getOutputVoltage() +
                ", maxOutputAmperage=" + getOutputAmperage() +
                ", amps=" + amps +
                '}';
    }
}
