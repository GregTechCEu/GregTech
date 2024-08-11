package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.IRoundRobinTraverseData;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class ItemRRTraverseData extends ItemTraverseData implements IRoundRobinTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final ArrayDeque<Object> cache;

    public ItemRRTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                              BlockPos sourcePos, EnumFacing inputFacing, @NotNull ArrayDeque<Object> cache) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        this.cache = cache;
    }

    @Override
    public @NotNull ArrayDeque<Object> getTraversalCache() {
        return cache;
    }

    @Override
    public boolean shouldSkipPath(FlowWorldPipeNetPath path) {
        return false;
    }
}
