package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Objects;

public class EnergyNetHandler implements IEnergyContainer {

    private EnergyNet net;
    private boolean transfer;
    private final TileEntityCable cable;
    private final EnumFacing facing;

    public EnergyNetHandler(EnergyNet net, TileEntityCable cable, EnumFacing facing) {
        this.net = Objects.requireNonNull(net);
        this.cable = Objects.requireNonNull(cable);
        this.facing = facing;
    }

    public void updateNetwork(EnergyNet net) {
        this.net = net;
    }

    public EnergyNet getNet() {
        return net;
    }

    @Override
    public long getInputPerSec() {
        return net.getEnergyFluxPerSec();
    }

    @Override
    public long getOutputPerSec() {
        return net.getEnergyFluxPerSec();
    }

    @Override
    public long getEnergyCanBeInserted() {
        return transfer ? 0 : getEnergyCapacity();
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (transfer) return 0;
        if (side == null) {
            if (facing == null) return 0;
            side = facing;
        }

        long amperesUsed = 0L;
        for (EnergyRoutePath path : net.getNetData(cable.getPos())) {
            if (path.getMaxLoss() >= voltage) {
                // Will lose all the energy with this path, so don't use it
                continue;
            }

            if (GTUtility.arePosEqual(cable.getPos(), path.getTargetPipePos()) && side == path.getTargetFacing()) {
                // Do not insert into source handler
                continue;
            }

            IEnergyContainer dest = path.getHandler();
            if (dest == null) continue;

            EnumFacing facing = path.getTargetFacing().getOpposite();
            if (!dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;

            long pathVoltage = voltage - path.getMaxLoss();
            boolean cableBroken = false;
            for (TileEntityCable cable : path.getPath()) {
                if (cable.getMaxVoltage() < voltage) {
                    int heat = (int) (Math.log(
                            GTUtility.getTierByVoltage(voltage) - GTUtility.getTierByVoltage(cable.getMaxVoltage())) *
                            45 + 36.5);
                    cable.applyHeat(heat);

                    cableBroken = cable.isInvalid();
                    if (cableBroken) {
                        // a cable burned away (or insulation melted)
                        break;
                    }

                    // limit transfer to cables max and void rest
                    pathVoltage = Math.min(cable.getMaxVoltage(), pathVoltage);
                }
            }

            if (cableBroken) continue;

            transfer = true;
            long amps = dest.acceptEnergyFromNetwork(facing, pathVoltage, amperage - amperesUsed);
            transfer = false;
            if (amps == 0) continue;

            amperesUsed += amps;
            long voltageTraveled = voltage;
            for (TileEntityCable cable : path.getPath()) {
                voltageTraveled -= cable.getNodeData().getLossPerBlock();
                if (voltageTraveled <= 0) break;

                if (!cable.isInvalid()) {
                    cable.incrementAmperage(amps, voltageTraveled);
                }
            }

            if (amperage == amperesUsed) break;
        }

        net.addEnergyFluxPerSec(amperesUsed * voltage);
        return amperesUsed;
    }

    private void burnCable(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.FIRE.getDefaultState());
        if (!world.isRemote) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    5 + world.rand.nextInt(3), 0.0, 0.0, 0.0, 0.1);
        }
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().getAmperage();
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().getVoltage();
    }

    @Override
    public long getEnergyCapacity() {
        return getInputVoltage() * getInputAmperage();
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        GTLog.logger.fatal("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(facing == null ? EnumFacing.UP : facing,
                energyToAdd / getInputAmperage(),
                energyToAdd / getInputVoltage()) * getInputVoltage();
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
