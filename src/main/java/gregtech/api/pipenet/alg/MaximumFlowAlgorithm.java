package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.PushRelabelMFImpl;

import java.util.List;
import java.util.Map;

/**
 * Secretly just {@link PushRelabelMFImpl}, but may be modified in the future.
 */
public final class MaximumFlowAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>>
                                       extends PushRelabelMFImpl<NodeG<PT, NDT>, NetEdge> {

    public MaximumFlowAlgorithm(Graph<NodeG<PT, NDT>, NetEdge> network) {
        super(network);
    }
}
