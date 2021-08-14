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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EnergyNetHandler implements IEnergyContainer {

    private final EnergyNet net;
    private final TileEntityCable cable;
    private final EnumFacing facing;

    public EnergyNetHandler(EnergyNet net, TileEntityCable cable, EnumFacing facing) {
        this.net = Objects.requireNonNull(net);
        this.cable = Objects.requireNonNull(cable);
        this.facing = facing;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if(side == null) {
            if(facing == null) return 0;
            side = facing;
        }

        long amperesUsed = 0L;
        List<RoutePath> paths = net.getNetData(cable.getPos());
        for (RoutePath path : paths) {
            if (path.getMaxLoss() >= voltage)
                continue;
            if(GTUtility.arePosEqual(cable.getPos(), path.getPipePos()) && side == path.getFaceToHandler()) {
                //Do not insert into source handler
                continue;
            }
            IEnergyContainer dest = path.getHandler(cable.getWorld());
            EnumFacing facing = path.getFaceToHandler().getOpposite();
            if (dest == null || !dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;
            long amps = dest.acceptEnergyFromNetwork(facing, voltage - path.getMaxLoss(), amperage - amperesUsed);
            amperesUsed += amps;
            for (TileEntityCable cable : path.getPath()) {
                boolean overVolt = cable.getMaxVoltage() < voltage;
                boolean overAmp = !cable.incrementAmperage(amps, voltage);
                if (overVolt || overAmp) {
                    burnCables(path.getPath(), overVolt, overAmp, amps);
                    break;
                }
            }
            if (amperage == amperesUsed)
                break;
        }
        return amperesUsed;
    }

    public void burnCables(Collection<TileEntityCable> cables, boolean overAmp, boolean overVolt, long amps) {
        if(cables == null || cables.size() == 0) return;
        World world = cables.iterator().next().getWorld();
        for(TileEntityCable cable : cables) {
            if(overVolt || (overAmp && amps > cable.getNodeData().amperage)) {
                BlockPos pos = cable.getPos();
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                if (!world.isRemote) {
                    ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            5 + world.rand.nextInt(3), 0.0, 0.0, 0.0, 0.1);
                }
            }
        }
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().amperage;
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().voltage;
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
