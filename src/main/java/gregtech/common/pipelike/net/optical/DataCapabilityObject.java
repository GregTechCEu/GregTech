package gregtech.common.pipelike.net.optical;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.data.IDataAccess;
import gregtech.api.capability.data.query.DataAccessFormat;
import gregtech.api.capability.data.query.DataQueryObject;
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

public class DataCapabilityObject implements IPipeCapabilityObject, IDataAccess {

    private final WorldPipeNetNode node;

    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);

    public DataCapabilityObject(@NotNull WorldPipeNetNode node) {
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
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        return accessData(queryObject, null);
    }

    private boolean accessData(@NotNull DataQueryObject queryObject, @Nullable EnumFacing facing) {
        if (tile == null) return false;

        NetPath path;
        if (node.getGroupUnsafe() == null || node.getGroupSafe().getNodes().size() == 1)
            path = new SingletonNetPath(node);
        else if (node.getGroupSafe().getData() instanceof PathCacheGroupData cache) {
            Set<NetNode> actives = node.getGroupSafe().getActiveNodes();
            if (actives.size() > 2) return false; // single-destination contract violated
            var iter = actives.iterator();
            NetNode target = iter.next();
            if (target == node) {
                if (!iter.hasNext()) return false; // no destinations
                target = iter.next();
            }
            path = cache.getOrCreate(node).getOrCompute(target);
            if (path == null) return false; // no path
        } else return false; // no cache to lookup with

        WorldPipeNetNode destination = path.getTargetNode();
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination == node && capability.getKey() == facing) continue; // anti insert-to-our-source logic
            IDataAccess access = capability.getValue()
                    .getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS,
                            capability.getKey().getOpposite());
            if (access != null) {
                queryObject.setShouldTriggerWalker(false);
                boolean cancelled = IOpticalTransferController.CONTROL
                        .get(destination.getTileEntity().getCoverHolder()
                                .getCoverAtSide(capability.getKey()))
                        .queryHandler(queryObject, access);
                if (queryObject.shouldTriggerWalker()) {
                    SlowActiveWalker.dispatch(tile.getWorld(), path, 1);
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
    public Capability<?>[] getCapabilities() {
        return WorldOpticalNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
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
