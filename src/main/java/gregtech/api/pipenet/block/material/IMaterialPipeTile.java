package gregtech.api.pipenet.block.material;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;

public interface IMaterialPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>, Edge extends NetEdge>
                                  extends IPipeTile<PipeType, NodeDataType, Edge> {

    Material getPipeMaterial();
}
