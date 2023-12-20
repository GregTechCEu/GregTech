package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.AbstractGroupData;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EnergyNetHandler implements IEnergyContainer {

    private final WorldEnergyNet net;
    private boolean transfer;
    private final TileEntityCable cable;
    private final EnumFacing facing;

    public EnergyNetHandler(WorldEnergyNet net, TileEntityCable cable, EnumFacing facing) {
        this.net = net;
        this.cable = cable;
        this.facing = facing;
    }

    public WorldEnergyNet getNet() {
        return net;
    }

    @Override
    public long getInputPerSec() {
        AbstractGroupData<Insulation, WireProperties> data = net.getGroup(cable.getPipePos()).getData();
        if (!(data instanceof EnergyGroupData e)) return 0;
        return e.getEnergyFluxPerSec();
    }

    @Override
    public long getOutputPerSec() {
        AbstractGroupData<Insulation, WireProperties> data = net.getGroup(cable.getPipePos()).getData();
        if (!(data instanceof EnergyGroupData e)) return 0;
        return e.getEnergyFluxPerSec();
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
        mainloop:
        for (NetPath<Insulation, WireProperties> routePath : net.getPaths(cable)) {
            routePath.resetFacingIterator();
            // weight = loss
            if (routePath.getWeight() >= voltage) {
                // Will lose all the energy with this path, so don't use it
                continue;
            }
            while (routePath.hasNextFacing()) {
                NetPath.FacedNetPath<Insulation, WireProperties> path = routePath.nextFacing();

                EnumFacing facing = path.facing.getOpposite();

                IEnergyContainer dest = path.getTargetTE().getCapability(
                        GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, facing);
                if (dest == null) continue;

                if (!dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;

                long pathVoltage = voltage - (long) routePath.getWeight();
                boolean cableBroken = false;
                for (NodeG<Insulation, WireProperties> node : path.getNodeList()) {
                    TileEntityCable cable = (TileEntityCable) node.getHeldMTE();
                    if (cable.getMaxVoltage() < voltage) {
                        int heat = (int) (Math.log(
                                GTUtility.getTierByVoltage(voltage) -
                                        GTUtility.getTierByVoltage(cable.getMaxVoltage())) *
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
                // TODO compress wire path operations into a single for loop
                for (NodeG<Insulation, WireProperties> node : path.getNodeList()) {
                    TileEntityCable cable = (TileEntityCable) node.getHeldMTE();
                    voltageTraveled -= cable.getNodeData().getLossPerBlock();
                    if (voltageTraveled <= 0) break;

                    if (!cable.isInvalid()) {
                        cable.incrementAmperage(amps, voltageTraveled);
                    }
                }

                if (amperage == amperesUsed) break mainloop;
            }
        }
        if (net.getGroup(this.cable.getPipePos()).getData() instanceof EnergyGroupData data)
            data.addEnergyFluxPerSec(amperesUsed * voltage);
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
