package gregtech.common.pipelike.cable.tile;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.RoutePath;
import gregtech.common.pipelike.cable.net.WorldENet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class CableEnergyContainer implements IEnergyContainer {

    private final IPipeTile<Insulation, WireProperties> tileEntityCable;
    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);
    private long lastCachedUpdate;
    private List<RoutePath> pathsCache;

    public CableEnergyContainer(IPipeTile<Insulation, WireProperties> tileEntityCable) {
        this.tileEntityCable = tileEntityCable;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        EnergyNet energyNet = getEnergyNet();
        if (energyNet == null) {
            return 0L;
        }
        List<RoutePath> paths = getPaths();
        long amperesUsed = 0;
        for (RoutePath routePath : paths) {
            if (routePath.totalLoss >= voltage) {
                continue; //do not emit if loss is too high
            }
            BlockPos destinationPos = routePath.destination;
            int openConnections = energyNet.getAllNodes().get(destinationPos).openConnections;
            amperesUsed += dispatchEnergyToNode(destinationPos, openConnections,
                    voltage - routePath.totalLoss, amperage - amperesUsed);

            if (voltage > routePath.minVoltage ||
                    amperesUsed > routePath.maxAmperage) {
                burnAllPaths(paths, voltage, amperage, amperesUsed);
                break; //break after burning all paths
            }

            if (amperesUsed == amperage) {
                break; //do not continue if all amperes are exhausted
            }
        }
        energyNet.incrementCurrentAmperage(amperage, voltage);
        return amperesUsed;
    }

    private void burnAllPaths(List<RoutePath> paths, long voltage, long amperage, long lastAmperage) {
        for (RoutePath pathToBurn : paths) {
            if (voltage > pathToBurn.minVoltage || amperage > pathToBurn.maxAmperage || lastAmperage > pathToBurn.maxAmperage) {
                pathToBurn.burnCablesInPath(tileEntityCable.getPipeWorld(), voltage, Math.max(amperage, lastAmperage));
            }
        }
    }

    private long dispatchEnergyToNode(BlockPos nodePos, int nodeOpenConnections, long voltage, long amperage) {
        long amperesUsed = 0L;
        //use pooled mutable to avoid creating new objects every tick
        World world = tileEntityCable.getPipeWorld();
        PooledMutableBlockPos blockPos = PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            if ((nodeOpenConnections & 1 << facing.getIndex()) == 0) {
                continue; //do not dispatch energy to blocked sides
            }
            blockPos.setPos(nodePos).move(facing);
            if (!world.isBlockLoaded(nodePos)) {
                continue; //do not allow cables to load chunks
            }
            TileEntity tileEntity = world.getTileEntity(blockPos);
            if (tileEntity == null || tileEntityCable.getPipeBlock().getPipeTileEntity(tileEntity) != null) {
                continue; //do not emit into other cable tile entities
            }
            IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, facing.getOpposite());
            if (energyContainer == null) continue;
            amperesUsed += energyContainer.acceptEnergyFromNetwork(facing.getOpposite(), voltage, amperage - amperesUsed);
            if (amperesUsed == amperage)
                break;
        }
        blockPos.release();
        return amperesUsed;
    }

    @Override
    public long getInputAmperage() {
        return tileEntityCable.getNodeData().amperage;
    }

    @Override
    public long getInputVoltage() {
        return tileEntityCable.getNodeData().voltage;
    }

    @Override
    public long getEnergyCapacity() {
        return getInputVoltage() * getInputAmperage();
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        //just a fallback case if somebody will call this method
        return acceptEnergyFromNetwork(EnumFacing.UP,
                energyToAdd / getInputVoltage(),
                energyToAdd / getInputAmperage()) * getInputVoltage();
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

    private void recomputePaths(EnergyNet energyNet) {
        this.lastCachedUpdate = energyNet.getLastUpdate();
        this.pathsCache = energyNet.computePatches(tileEntityCable.getPipePos());
    }

    private List<RoutePath> getPaths() {
        EnergyNet energyNet = getEnergyNet();
        if (energyNet == null) {
            return Collections.emptyList();
        }
        if (pathsCache == null || energyNet.getLastUpdate() > lastCachedUpdate) {
            recomputePaths(energyNet);
        }
        return pathsCache;
    }

    private EnergyNet getEnergyNet() {
        EnergyNet currentEnergyNet = this.currentEnergyNet.get();
        if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                currentEnergyNet.containsNode(tileEntityCable.getPipePos()))
            return currentEnergyNet; //return current net if it is still valid
        WorldENet worldENet = (WorldENet) tileEntityCable.getPipeBlock().getWorldPipeNet(tileEntityCable.getPipeWorld());
        currentEnergyNet = worldENet.getNetFromPos(tileEntityCable.getPipePos());
        if (currentEnergyNet != null) {
            this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
        }
        return currentEnergyNet;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
