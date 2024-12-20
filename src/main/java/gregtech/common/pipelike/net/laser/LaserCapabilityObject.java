package gregtech.common.pipelike.net.laser;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.graphnet.group.PathCacheGroupData;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.path.SingletonNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.common.covers.CoverShutter;
import gregtech.common.pipelike.net.SlowActiveWalker;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Set;

public class LaserCapabilityObject implements IPipeCapabilityObject, ILaserRelay {

    public static final int ACTIVE_KEY = 122;

    protected final WorldPipeNode node;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);

    private boolean transmitting;

    public LaserCapabilityObject(@NotNull WorldPipeNode node) {
        this.node = node;
        for (EnumFacing facing : EnumFacing.VALUES) {
            wrappers.put(facing, new Wrapper(facing));
        }
    }

    @Override
    public void init(@NotNull PipeTileEntity tile, @NotNull PipeCapabilityWrapper wrapper) {
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
            Set<NetNode> actives = node.getGroupSafe().getNodesUnderKey(ACTIVE_KEY);
            if (actives.size() > 2) return 0; // single-destination contract violated
            var iter = actives.iterator();
            NetNode target = iter.next();
            if (target == node) {
                if (!iter.hasNext()) return 0; // no destinations
                target = iter.next();
            }
            if (!(target instanceof WorldPipeNode)) return 0; // useless target
            path = cache.getOrCreate(node).getOrCompute(target);
            if (path == null) return 0; // no path
        } else return 0; // no cache to lookup with

        long available = laserAmperage;
        WorldPipeNode destination = (WorldPipeNode) path.getTargetNode();
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination == node && capability.getKey() == facing) continue; // anti insert-to-our-source logic
            ILaserRelay laser = capability.getValue()
                    .getCapability(GregtechTileCapabilities.CAPABILITY_LASER, capability.getKey().getOpposite());
            if (laser != null && !(destination.getTileEntity().getCoverHolder()
                    .getCoverAtSide(capability.getKey()) instanceof CoverShutter)) {
                long transmitted = laser.receiveLaser(laserVoltage, laserAmperage);
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
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
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
