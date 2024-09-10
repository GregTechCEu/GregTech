package gregtech.api.graphnet.pipenet.traverse;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Function;

public class FlowManagerMap extends Object2ObjectOpenHashMap<WorldPipeNetNode, ITileFlowManager> {

    private final Function<WorldPipeNetNode, ITileFlowManager> newSupplier;

    public FlowManagerMap(Function<WorldPipeNetNode, ITileFlowManager> newSupplier) {
        this.newSupplier = newSupplier;
    }

    public ITileFlowManager access(WorldPipeNetNode node) {
        return computeIfAbsent(node, newSupplier);
    }
}
