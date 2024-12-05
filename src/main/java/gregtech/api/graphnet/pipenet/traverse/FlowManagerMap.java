package gregtech.api.graphnet.pipenet.traverse;

import gregtech.api.graphnet.pipenet.WorldPipeNode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Function;

public class FlowManagerMap extends Object2ObjectOpenHashMap<WorldPipeNode, ITileFlowManager> {

    private final Function<WorldPipeNode, ITileFlowManager> newSupplier;

    public FlowManagerMap(Function<WorldPipeNode, ITileFlowManager> newSupplier) {
        this.newSupplier = newSupplier;
    }

    public ITileFlowManager access(WorldPipeNode node) {
        return computeIfAbsent(node, newSupplier);
    }
}
