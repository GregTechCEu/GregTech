package gregtech.common.pipelike.laser.tile;

//import gregicadditions.capabilities.GregicAdditionsCapabilities;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import gregtech.common.pipelike.laser.tile.LaserProperties;
import gregtech.common.pipelike.laser.tile.LaserSize;
import gregtech.common.pipelike.laser.net.LaserPipeNet;
import gregtech.common.pipelike.laser.net.LaserPath;
import gregtech.common.pipelike.laser.net.WorldLaserNet;
import gregtech.api.pipenet.tile.IPipeTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class CableLaserContainer implements LaserContainer{

    private final IPipeTile<LaserSize, LaserProperties> tileEntityCable;
    private WeakReference<LaserPipeNet> Lasernet = new WeakReference<>(null);
    private long lastCachedUpdate;
    private List<LaserPath> pathsCache;

    public CableLaserContainer(IPipeTile<LaserSize, LaserProperties> tileEntityCable) {
        this.tileEntityCable = tileEntityCable;
    }

    @Override
    public long acceptLaserFromNetwork(EnumFacing side, long qubit, long parallel) {
        LaserPipeNet LaserPipeNet = getLaserNet();
        if (LaserPipeNet == null) {
            return 0L;
        }
        List<LaserPath> paths = getPaths();
        long currentParallel = 0;
        for (LaserPath routePath : paths) {
            BlockPos destinationPos = routePath.destination;
            int blockedConnections = LaserPipeNet.getAllNodes().get(destinationPos).blockedConnections;
            currentParallel += dispatchLasertToNode(destinationPos, blockedConnections, qubit, parallel - currentParallel);

            if (currentParallel == parallel) {
                break; //do not continue if all amperes are exhausted
            }
        }
        LaserPipeNet.incrementCurrentAmperage(parallel, qubit);
        return currentParallel;
    }


    private long dispatchLasertToNode(BlockPos nodePos, int nodeBlockedConnections, long voltage, long amperage) {
        long currentParallel = 0L;
        //use pooled mutable to avoid creating new objects every tick
        World world = tileEntityCable.getPipeWorld();
        PooledMutableBlockPos blockPos = PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            if ((nodeBlockedConnections & 1 << facing.getIndex()) > 0) {
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
            LaserContainer laserContainer = tileEntity.getCapability(GregtechTileCapabilities.LASER_CAPABILITY, facing.getOpposite());
            if (laserContainer == null) continue;
            currentParallel += laserContainer.acceptLaserFromNetwork(facing.getOpposite(), voltage, amperage - currentParallel);
            if (currentParallel == amperage)
                break;
        }
        blockPos.release();
        return currentParallel;
    }

    @Override
    public long getInputParallel() {
        return tileEntityCable.getNodeData().parallel;
    }

    @Override
    public long getInputLaser() {
        return tileEntityCable.getNodeData().laserVoltage;
    }

    @Override
    public long getLaserCapacity() {
        return getInputLaser() * getInputParallel();
    }

    @Override
    public long changeLaser(long energyToAdd) {
        //just a fallback case if somebody will call this method
        return acceptLaserFromNetwork(EnumFacing.UP,
                energyToAdd / getInputLaser(),
                energyToAdd / getInputParallel()) * getInputLaser();
    }

    @Override
    public boolean outputsLaser(EnumFacing side) {
        return true;
    }

    @Override
    public boolean inputsLaser(EnumFacing side) {
        return true;
    }

    @Override
    public long getLaserStored() {
        return 0;
    }

    private void recomputePaths(LaserPipeNet lasernet) {
        this.lastCachedUpdate = lasernet.getLastUpdate();
        this.pathsCache = lasernet.computePatches(tileEntityCable.getPipePos());
    }

    private List<LaserPath> getPaths() {
        LaserPipeNet lasernet = getLaserNet();
        if (lasernet == null) {
            return Collections.emptyList();
        }
        if (pathsCache == null || lasernet.getLastUpdate() > lastCachedUpdate) {
            recomputePaths(lasernet);
        }
        return pathsCache;
    }

    private LaserPipeNet getLaserNet() {
        LaserPipeNet lasernet = this.Lasernet.get();
        if (lasernet != null && lasernet.isValid() &&
                lasernet.containsNode(tileEntityCable.getPipePos()))
            return lasernet; //return current net if it is still valid
        WorldLaserNet worldOpticalFiberNet = (WorldLaserNet) tileEntityCable.getPipeBlock().getWorldPipeNet(tileEntityCable.getPipeWorld());
        lasernet = worldOpticalFiberNet.getNetFromPos(tileEntityCable.getPipePos());
        if (lasernet != null) {
            this.Lasernet = new WeakReference<>(lasernet);
        }
        return lasernet;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}

