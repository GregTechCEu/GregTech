package gregtech.common.pipelike.net.optical;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.group.PathCacheGroupData;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.traverse.iter.NetBreadthIterator;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldOpticalNet extends WorldPipeNet {

    public static final Capability<?>[] CAPABILITIES = new Capability[] {
            GregtechTileCapabilities.CAPABILITY_DATA_ACCESS };

    private static final String DATA_ID_BASE = "gregtech.world_optical_net";

    public static @NotNull WorldOpticalNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldOpticalNet net = (WorldOpticalNet) world.loadData(WorldOpticalNet.class, DATA_ID);
        if (net == null) {
            net = new WorldOpticalNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        net.setWorld(world);
        return net;
    }

    public WorldOpticalNet(String name) {
        super(name, false);
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new DataCapabilityObject(node) };
    }

    @Override
    public @Nullable GroupData getBlankGroupData() {
        return new PathCacheGroupData(NetBreadthIterator::new);
    }

    @Override
    public int getNetworkID() {
        return 4;
    }
}
