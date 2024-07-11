package gregtech.common.pipelike.net;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;

import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class EnergyNet extends WorldPipeNet {

    private static final String DATA_ID_BASE = "gregtech.energy_net";

    public static EnergyNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        EnergyNet net = (EnergyNet) world.loadData(EnergyNet.class, DATA_ID);
        if (net == null) {
            net = new EnergyNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        return net;
    }

    public EnergyNet(String name) {
        super(name, DynamicWeightsShortestPathsAlgorithm::new, false);
    }

    public Iterator<FlowWorldPipeNetPath> getPaths(NetNode node) {
        return backer.getPaths(node, FlowWorldPipeNetPath.MAPPER);
    }

    @Override
    public @NotNull NetEdge getNewEdge() {
        return new NetFlowEdge(1);
    }
}
