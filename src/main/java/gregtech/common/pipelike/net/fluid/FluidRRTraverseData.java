package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.IRoundRobinTraverseData;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class FluidRRTraverseData extends FluidTraverseData
                                 implements IRoundRobinTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final ArrayDeque<Object> cache;

    public FluidRRTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
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
