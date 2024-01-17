package gregtech.api.pipenet;

import gregtech.api.pipenet.alg.MaximumFlowAlgorithm;
import gregtech.api.pipenet.block.IPipeType;

public abstract class WorldPipeFlowNetG<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldPipeNetG<NodeDataType, PipeType> {

    /**
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections.
     */
    public WorldPipeFlowNetG(String name, boolean isDirected) {
        super(name, isDirected, false);
    }

    @Override
    protected void rebuildNetAlgorithm() {
        this.netAlgorithm.setAlg(new MaximumFlowAlgorithm<>(pipeGraph, netAlgorithm));
        this.validAlgorithmInstance = true;
    }
}
