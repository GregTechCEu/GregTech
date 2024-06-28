package gregtech.api.pipenet.graph;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.WorldPipeNetBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.pipenet.edge.SimulatorKey;
import gregtech.api.pipenet.predicate.IPredicateTestObject;

import org.jetbrains.annotations.ApiStatus;
import org.jgrapht.Graph;

public interface ICustomGraph<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>, E extends NetEdge>
                             extends Graph<NetNode<PT, NDT, E>, E> {

    @ApiStatus.Internal
    void setOwningNet(WorldPipeNetBase<NDT, PT, E> net);

    void prepareForDynamicWeightAlgorithmRun(IPredicateTestObject testObject, SimulatorKey simulator,
                                             long queryTick);
}
