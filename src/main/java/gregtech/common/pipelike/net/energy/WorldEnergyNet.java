package gregtech.common.pipelike.net.energy;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.traverse.iter.NetClosestIterator;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

public final class WorldEnergyNet extends WorldPipeNet {

    public static final Capability<?>[] CAPABILITIES = new Capability[] {
            GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER };

    private static final String DATA_ID_BASE = "gregtech.world_energy_net";

    public static @NotNull WorldEnergyNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldEnergyNet net = (WorldEnergyNet) world.loadData(WorldEnergyNet.class, DATA_ID);
        if (net == null) {
            net = new WorldEnergyNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        net.setWorld(world);
        return net;
    }

    public WorldEnergyNet(String name) {
        super(name, false);
    }

    @Override
    public PipeCapabilityWrapper buildCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node) {
        Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> map = new Object2ObjectOpenHashMap<>();
        map.put(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, new EnergyCapabilityObject(node));
        return new PipeCapabilityWrapper(owner, node, map, 0, EnergyCapabilityObject.ACTIVE_KEY);
    }

    @Override
    public GroupData getBlankGroupData() {
        return new EnergyGroupData(NetClosestIterator::new);
    }

    @Override
    public int getNetworkID() {
        return 0;
    }
}
