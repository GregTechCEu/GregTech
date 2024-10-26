package gregtech.common.pipelike.net.laser;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.group.PathCacheGroupData;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.path.SingletonNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.common.pipelike.net.SlowActiveWalker;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Set;

public class LaserCapabilityObject implements IPipeCapabilityObject, ILaserRelay {

    protected final WorldPipeNetNode node;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);

    private boolean transmitting;

    public LaserCapabilityObject(@NotNull WorldPipeNetNode node) {
        this.node = node;
        for (EnumFacing facing : EnumFacing.VALUES) {
            wrappers.put(facing, new Wrapper(facing));
        }
    }

    @Override
    public void setTile(@Nullable PipeTileEntity tile) {
        this.tile = tile;
    }

    @Override
    public long receiveLaser(long laserVoltage, long laserAmperage) {
        return receiveLaser(laserVoltage, laserAmperage, null);
    }

    protected long receiveLaser(long laserVoltage, long laserAmperage, EnumFacing facing) {
        if (tile == null || this.transmitting) return 0;
        this.transmitting = true;

        NetPath path;
        if (node.getGroupUnsafe() == null || node.getGroupSafe().getNodes().size() == 1)
            path = new SingletonNetPath(node);
        else if (node.getGroupSafe().getData() instanceof PathCacheGroupData cache) {
            Set<NetNode> actives = node.getGroupSafe().getActiveNodes();
            if (actives.size() > 2) return 0; // single-destination contract violated
            var iter = actives.iterator();
            NetNode target = iter.next();
            if (target == node) {
                if (!iter.hasNext()) return 0; // no destinations
                target = iter.next();
            }
            path = cache.getOrCreate(node).getOrCompute(target);
            if (path == null) return 0; // no path
        } else return 0; // no cache to lookup with

        long available = laserAmperage;
        WorldPipeNetNode destination = path.getTargetNode();
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination == node && capability.getKey() == facing) continue; // anti insert-to-our-source logic
            ILaserRelay laser = capability.getValue()
                    .getCapability(GregtechTileCapabilities.CAPABILITY_LASER, capability.getKey().getOpposite());
            if (laser != null) {
                long transmitted = ILaserTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(laserVoltage, laserAmperage, laser);
                if (transmitted > 0) {
                    SlowActiveWalker.dispatch(tile.getWorld(), path, 1, 2, 2);
                    available -= transmitted;
                    if (available <= 0) {
                        this.transmitting = false;
                        return laserAmperage;
                    }
                }
            }
        }
        this.transmitting = false;

        return laserAmperage - available;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldLaserNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    protected class Wrapper implements ILaserRelay {

        private final EnumFacing facing;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public long receiveLaser(long laserVoltage, long laserAmperage) {
            return LaserCapabilityObject.this.receiveLaser(laserVoltage, laserAmperage, facing);
        }
    }
}
