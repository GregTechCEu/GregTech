package gregtech.api.graphnet.traverseold;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface TraverseDataProvider<D extends ITraverseData<?, ?>, T extends IPredicateTestObject> {

    D of(IGraphNet net, T testObject, SimulatorKey simulator, long queryTick,
         BlockPos sourcePos, EnumFacing inputFacing);
}
