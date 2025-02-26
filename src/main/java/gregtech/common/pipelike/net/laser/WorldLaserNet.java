package gregtech.common.pipelike.net.laser;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.group.PathCacheGroupData;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.traverse.NetBreadthIterator;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldLaserNet extends WorldPipeNet {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { GregtechTileCapabilities.CAPABILITY_LASER };

    private static final String DATA_ID_BASE = "gregtech.world_laser_net";

    public static @NotNull WorldLaserNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldLaserNet net = (WorldLaserNet) world.loadData(WorldLaserNet.class, DATA_ID);
        if (net == null) {
            net = new WorldLaserNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        net.setWorld(world);
        return net;
    }

    public WorldLaserNet(String name) {
        super(name, false);
    }

    @Override
    public PipeCapabilityWrapper buildCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node) {
        Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> map = new Object2ObjectOpenHashMap<>();
        map.put(GregtechTileCapabilities.CAPABILITY_LASER, new LaserCapabilityObject(node));
        return new PipeCapabilityWrapper(owner, node, map, 0, LaserCapabilityObject.ACTIVE_KEY);
    }

    @Override
    public @Nullable GroupData getBlankGroupData() {
        return new PathCacheGroupData(NetBreadthIterator::new);
    }

    @Override
    public int getNetworkID() {
        return 3;
    }
}
