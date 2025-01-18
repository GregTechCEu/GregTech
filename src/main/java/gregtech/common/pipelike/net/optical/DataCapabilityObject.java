package gregtech.common.pipelike.net.optical;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.data.IDataAccess;
import gregtech.api.capability.data.query.DataAccessFormat;
import gregtech.api.capability.data.query.DataQueryObject;
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

public class DataCapabilityObject implements IPipeCapabilityObject, IDataAccess {

    public static final int ACTIVE_KEY = 122;

    private final WorldPipeNode node;

    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);

    public DataCapabilityObject(@NotNull WorldPipeNode node) {
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
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        return accessData(queryObject, null);
    }

    private boolean accessData(@NotNull DataQueryObject queryObject, @Nullable EnumFacing facing) {
        if (tile == null) return false;

        NetPath path;
        if (node.getGroupUnsafe() == null || node.getGroupSafe().getNodes().size() == 1)
            path = new SingletonNetPath(node);
        else if (node.getGroupSafe().getData() instanceof PathCacheGroupData cache) {
            Set<NetNode> actives = node.getGroupSafe().getNodesUnderKey(ACTIVE_KEY);
            if (actives.size() > 2) return false; // single-destination contract violated
            var iter = actives.iterator();
            NetNode target = iter.next();
            if (target == node) {
                if (!iter.hasNext()) return false; // no destinations
                target = iter.next();
            }
            if (!(target instanceof WorldPipeNode)) return false; // useless target
            path = cache.getOrCreate(node).getOrCompute(target);
            if (path == null) return false; // no path
        } else return false; // no cache to lookup with

        WorldPipeNode destination = (WorldPipeNode) path.getTargetNode();
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination == node && capability.getKey() == facing) continue; // anti insert-to-our-source logic
            IDataAccess access = capability.getValue()
                    .getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS,
                            capability.getKey().getOpposite());
            if (access != null && !(destination.getTileEntity().getCoverHolder()
                    .getCoverAtSide(capability.getKey()) instanceof CoverShutter)) {
                queryObject.setShouldTriggerWalker(false);
                boolean cancelled = access.accessData(queryObject);
                if (queryObject.shouldTriggerWalker()) {
                    // since we are a pull-based system, we need to reverse the path for it to look correct
                    SlowActiveWalker.dispatch(tile.getWorld(), path.reversed(), 1, 1, 5);
                }
                if (cancelled) return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull DataAccessFormat getFormat() {
        return DataAccessFormat.UNIVERSAL;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {
            return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    protected class Wrapper implements IDataAccess {

        private final EnumFacing facing;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public boolean accessData(@NotNull DataQueryObject queryObject) {
            return DataCapabilityObject.this.accessData(queryObject, facing);
        }

        @Override
        public @NotNull DataAccessFormat getFormat() {
            return DataCapabilityObject.this.getFormat();
        }

        @Override
        public boolean supportsQuery(@NotNull DataQueryObject queryObject) {
            return DataCapabilityObject.this.supportsQuery(queryObject);
        }
    }
}
