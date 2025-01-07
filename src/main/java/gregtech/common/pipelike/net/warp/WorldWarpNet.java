package gregtech.common.pipelike.net.warp;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldWarpNet extends WorldPipeNet {

    private static final String DATA_ID_BASE = "gregtech.world_warp_net";

    public static @NotNull WorldWarpNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldWarpNet net = (WorldWarpNet) world.loadData(WorldWarpNet.class, DATA_ID);
        if (net == null) {
            net = new WorldWarpNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        net.setWorld(world);
        return net;
    }

    public WorldWarpNet(String name) {
        super(name, true);
    }

    @Override
    public PipeCapabilityWrapper buildCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node) {
        Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> map = new Object2ObjectOpenHashMap<>();
        map.put(GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER, new WarpCapabilityObject(node));
        return new PipeCapabilityWrapper(owner, node, map, 0, WarpCapabilityObject.ACTIVE_KEY);
    }

    @Override
    public int getNetworkID() {
        return 857984;
    }

    @Override
    public @Nullable GroupData getBlankGroupData() {
        return null;
    }
}
